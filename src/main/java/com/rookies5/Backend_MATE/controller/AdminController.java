package com.rookies5.Backend_MATE.controller;

import com.rookies5.Backend_MATE.dto.response.ProjectResponseDto;
import com.rookies5.Backend_MATE.dto.response.UserResponseDto;
import com.rookies5.Backend_MATE.entity.Project;
import com.rookies5.Backend_MATE.mapper.ProjectMapper;
import com.rookies5.Backend_MATE.mapper.UserMapper;
import com.rookies5.Backend_MATE.repository.ProjectRepository;
import com.rookies5.Backend_MATE.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    // ✅ 대시보드
    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(defaultValue = "0") int userPage,
            @RequestParam(defaultValue = "0") int projectPage,
            Model model) {

        int size = 5;

        // ✅ 수정: 회원 조회도 soft delete 포함 (findAll -> findAllIncludingDeleted)
        // ⚠️ 주의: Native Query 정렬은 DB 컬럼명인 "created_at" 권장
        Page<UserResponseDto> users = userRepository
                .findAllIncludingDeleted(PageRequest.of(userPage, size, Sort.by("created_at").descending()))
                .map(UserMapper::mapToUserResponse);

        // ✅ 유지: 프로젝트 조회 (이미 findAllIncludingDeleted 사용 중)
        Page<ProjectResponseDto> projects = projectRepository
                .findAllIncludingDeleted(PageRequest.of(projectPage, size, Sort.by("created_at").descending()))
                .map(project -> {
                    ProjectResponseDto dto = ProjectMapper.mapToResponse(project);
                    dto.setDeleted(project.getDeletedAt() != null);
                    return dto;
                });

        List<String> logs = List.of("관리자 접속", "회원 가입 발생", "게시글 생성");

        // ✅ 수정: 카운트도 전체 데이터 기준 (count -> countIncludingDeleted)
        model.addAttribute("userCount", userRepository.countIncludingDeleted());
        model.addAttribute("projectCount", projectRepository.countIncludingDeleted());
        model.addAttribute("users", users);
        model.addAttribute("projects", projects);
        model.addAttribute("logs", logs);

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String userManagement(
            @RequestParam(defaultValue = "0") int userPage,
            Model model
    ) {
        int size = 5;

        // ✅ 수정: 회원 관리 페이지도 탈퇴 회원 포함
        Page<UserResponseDto> users = userRepository
                .findAllIncludingDeleted(PageRequest.of(userPage, size, Sort.by("created_at").descending()))
                .map(UserMapper::mapToUserResponse);

        model.addAttribute("users", users);
        model.addAttribute("userCount", userRepository.countIncludingDeleted());

        return "admin/users";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }

    // ✅ 회원 상세 (모달용)
    @GetMapping("/users/{id}")
    @ResponseBody
    public UserResponseDto getUserDetail(@PathVariable Long id) {
        // ✅ 수정: findById -> findByIdIncludingDeleted
        return userRepository.findByIdIncludingDeleted(id)
                .map(UserMapper::mapToUserResponse)
                .orElseThrow(() -> new RuntimeException("해당 회원을 찾을 수 없습니다."));
    }

    // ✅ 프로젝트 상세 (모달용)
    @GetMapping("/projects/{id}")
    @ResponseBody
    public ProjectResponseDto getProjectDetail(@PathVariable Long id) {
        // ✅ 수정: findById -> findByIdIncludingDeleted
        Project project = projectRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new RuntimeException("해당 프로젝트를 찾을 수 없습니다."));
        return ProjectMapper.mapToResponse(project);
    }

    // ✅ 회원 삭제
    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/dashboard";
    }

    // ✅ 프로젝트 삭제 (soft delete)
    @PostMapping("/projects/delete/{id}")
    public String deleteProject(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int userPage,
            @RequestParam(defaultValue = "0") int projectPage
    ) {
        // ✅ 수정: findById -> findByIdIncludingDeleted
        Project project = projectRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new RuntimeException("프로젝트 없음"));
        project.softDelete();
        projectRepository.save(project);

        return "redirect:/admin/dashboard?userPage=" + userPage + "&projectPage=" + projectPage;
    }

    // ✅ 프로젝트 복구
    @PostMapping("/projects/restore/{id}")
    public String restoreProject(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int userPage,
            @RequestParam(defaultValue = "0") int projectPage
    ) {
        // ✅ 핵심 수정: findById -> findByIdIncludingDeleted (@Where 무시 필수)
        Project project = projectRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new RuntimeException("프로젝트 없음"));
        project.setDeletedAt(null);
        projectRepository.save(project);

        return "redirect:/admin/dashboard?userPage=" + userPage + "&projectPage=" + projectPage;
    }

    // ✅ 검색 기능
    @GetMapping("/search")
    public String search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int userPage,
            @RequestParam(defaultValue = "0") int projectPage,
            Model model) {

        int size = 5;

        // ✅ 수정: 전체 회원 필터링 (findAll -> findAllIncludingDeletedList)
        List<UserResponseDto> userList = userRepository.findAllIncludingDeletedList().stream()
                .filter(u -> u.getNickname().contains(keyword) || u.getEmail().contains(keyword))
                .map(UserMapper::mapToUserResponse)
                .toList();

        // ✅ 수정: 전체 프로젝트 필터링 (findAllIncludingDeletedList 사용)
        List<ProjectResponseDto> projectList = projectRepository.findAllIncludingDeletedList().stream()
                .filter(p -> p.getTitle().contains(keyword))
                .map(ProjectMapper::mapToResponse)
                .toList();

        Page<UserResponseDto> users = new PageImpl<>(userList, PageRequest.of(userPage, size), userList.size());
        Page<ProjectResponseDto> projects = new PageImpl<>(projectList, PageRequest.of(projectPage, size), projectList.size());

        List<String> logs = List.of("관리자 접속", "회원 가입 발생", "게시글 생성");

        model.addAttribute("userCount", userRepository.countIncludingDeleted());
        model.addAttribute("projectCount", projectRepository.countIncludingDeleted());
        model.addAttribute("users", users);
        model.addAttribute("projects", projects);
        model.addAttribute("logs", logs);
        model.addAttribute("keyword", keyword);

        return "admin/dashboard";
    }
}