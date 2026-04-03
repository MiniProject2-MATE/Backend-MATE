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

    // ✅ 대시보드 (페이징 적용 핵심)
    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(defaultValue = "0") int userPage,
            @RequestParam(defaultValue = "0") int projectPage,
            Model model) {

        int size = 5;

        // 회원 페이지
        Page<UserResponseDto> users = userRepository
                .findAll(PageRequest.of(userPage, size, Sort.by("createdAt").descending()))
                .map(UserMapper::mapToUserResponse);

        // 프로젝트 페이지
        Page<ProjectResponseDto> projects = projectRepository
                .findAll(PageRequest.of(projectPage, size, Sort.by("createdAt").descending()))
                .map(ProjectMapper::mapToResponse);

        // 로그 (임시)
        List<String> logs = List.of("관리자 접속", "회원 가입 발생", "게시글 생성");

        // 데이터 전달
        model.addAttribute("userCount", userRepository.count());
        model.addAttribute("projectCount", projectRepository.count());

        model.addAttribute("users", users);
        model.addAttribute("projects", projects);
        model.addAttribute("logs", logs);

        return "admin/dashboard";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }

    // ✅ 회원 리스트 (10개씩)
    @GetMapping("/users")
    public String userList(
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<UserResponseDto> result = userRepository
                .findAll(PageRequest.of(page, 10, Sort.by("createdAt").descending()))
                .map(UserMapper::mapToUserResponse);

        model.addAttribute("users", result);

        return "admin/users";
    }

    // ✅ 프로젝트 리스트
    @GetMapping("/projects")
    public String projectList(
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<ProjectResponseDto> result = projectRepository
                .findAll(PageRequest.of(page, 10, Sort.by("createdAt").descending()))
                .map(ProjectMapper::mapToResponse);

        model.addAttribute("projects", result);

        return "admin/projects";
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

    // ✅ 프로젝트 삭제
    @PostMapping("/projects/delete/{id}")
    public String deleteProject(@PathVariable Long id) {
        projectRepository.deleteById(id);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/search")
    public String search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int userPage,
            @RequestParam(defaultValue = "0") int projectPage,
            Model model) {

        int size = 5;

        // 👉 전체 조회 후 필터
        List<UserResponseDto> userList = userRepository.findAll().stream()
                .filter(u -> u.getNickname().contains(keyword) || u.getEmail().contains(keyword))
                .map(UserMapper::mapToUserResponse)
                .toList();

        List<ProjectResponseDto> projectList = projectRepository.findAll().stream()
                .filter(p -> p.getTitle().contains(keyword))
                .map(ProjectMapper::mapToResponse)
                .toList();

        // 👉 Page로 변환 (이게 핵심)
        Page<UserResponseDto> users = new PageImpl<>(
                userList,
                PageRequest.of(userPage, size),
                userList.size()
        );

        Page<ProjectResponseDto> projects = new PageImpl<>(
                projectList,
                PageRequest.of(projectPage, size),
                projectList.size()
        );

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