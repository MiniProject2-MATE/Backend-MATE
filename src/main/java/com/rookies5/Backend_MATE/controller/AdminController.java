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

        // 회원 조회
        Page<UserResponseDto> users = userRepository
                .findAll(PageRequest.of(userPage, size, Sort.by("createdAt").descending()))
                .map(UserMapper::mapToUserResponse);

        // ✅ 수정: soft delete 포함 프로젝트 조회
        Page<ProjectResponseDto> projects = projectRepository
                .findAllIncludingDeleted(PageRequest.of(projectPage, size, Sort.by("createdAt").descending()))
                .map(project -> {
                    ProjectResponseDto dto = ProjectMapper.mapToResponse(project);
                    dto.setDeleted(project.getDeletedAt() != null); // soft delete 여부 표시
                    return dto;
                });

        List<String> logs = List.of("관리자 접속", "회원 가입 발생", "게시글 생성");

        model.addAttribute("userCount", userRepository.count());
        model.addAttribute("projectCount", projectRepository.count());
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
        int size = 5; // 한 페이지당 표시할 회원 수

        // 회원 조회 (페이지네이션)
        Page<UserResponseDto> users = userRepository
                .findAll(PageRequest.of(userPage, size, Sort.by("createdAt").descending()))
                .map(UserMapper::mapToUserResponse);

        model.addAttribute("users", users);
        model.addAttribute("userCount", userRepository.count());

        return "admin/users"; // templates/admin/users.html
    }
    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }

    // ✅ 회원 상세 (모달용)
    @GetMapping("/users/{id}")
    @ResponseBody
    public UserResponseDto getUserDetail(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(UserMapper::mapToUserResponse)
                .orElseThrow(() -> new RuntimeException("해당 회원을 찾을 수 없습니다."));
    }

    // ✅ 프로젝트 상세 (모달용)
    @GetMapping("/projects/{id}")
    @ResponseBody
    public ProjectResponseDto getProjectDetail(@PathVariable Long id) {
        Project project = projectRepository.findById(id)
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
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("프로젝트 없음"));
        project.softDelete();
        projectRepository.save(project);

        // ✅ 수정: 페이지 정보 유지
        return "redirect:/admin/dashboard?userPage=" + userPage + "&projectPage=" + projectPage;
    }

    // ✅ 프로젝트 복구
    @PostMapping("/projects/restore/{id}")
    public String restoreProject(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int userPage,
            @RequestParam(defaultValue = "0") int projectPage
    ) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("프로젝트 없음"));
        project.setDeletedAt(null);
        projectRepository.save(project);

        // ✅ 수정: 페이지 정보 유지
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

        // 전체 회원 필터링
        List<UserResponseDto> userList = userRepository.findAll().stream()
                .filter(u -> u.getNickname().contains(keyword) || u.getEmail().contains(keyword))
                .map(UserMapper::mapToUserResponse)
                .toList();

        // ✅ 수정: soft delete 포함 프로젝트 필터링
        List<ProjectResponseDto> projectList = projectRepository.findAllIncludingDeleted(PageRequest.of(0, Integer.MAX_VALUE))
                .map(ProjectMapper::mapToResponse)
                .stream()
                .filter(p -> p.getTitle().contains(keyword))
                .toList();

        // Page로 변환
        Page<UserResponseDto> users = new PageImpl<>(userList, PageRequest.of(userPage, size), userList.size());
        Page<ProjectResponseDto> projects = new PageImpl<>(projectList, PageRequest.of(projectPage, size), projectList.size());

        List<String> logs = List.of("관리자 접속", "회원 가입 발생", "게시글 생성");

        model.addAttribute("userCount", userRepository.count());
        model.addAttribute("projectCount", projectRepository.count());
        model.addAttribute("users", users);
        model.addAttribute("projects", projects);
        model.addAttribute("logs", logs);
        model.addAttribute("keyword", keyword);

        return "admin/dashboard";
    }

}