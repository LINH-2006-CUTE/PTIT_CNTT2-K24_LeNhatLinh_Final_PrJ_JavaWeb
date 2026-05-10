package com.btvn.projectfinal.controller;

import com.btvn.projectfinal.model.dto.LoginDTO;
import com.btvn.projectfinal.model.dto.RegisterDTO;
import com.btvn.projectfinal.model.entity.User;
import com.btvn.projectfinal.repository.DepartmentRepository;
import com.btvn.projectfinal.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;       // ✅ đúng
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;                        // ✅ đúng
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final DepartmentRepository departmentRepository;
    private final AuthenticationManager authenticationManager;

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginDTO", new LoginDTO());
        return "auth/login";
    }

    @PostMapping("/login-validate")
    public String loginValidate(
            @Valid @ModelAttribute("loginDTO") LoginDTO dto,
            BindingResult bindingResult,
            Model model,
            HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            return "auth/login";
        }

        try {
            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword());
            Authentication authentication = authenticationManager.authenticate(token);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            HttpSession session = request.getSession(true);
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext()
            );

            return "redirect:/dashboard";

        } catch (BadCredentialsException e) {
            model.addAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không đúng!");
            model.addAttribute("loginDTO", dto);
            return "auth/login";
        } catch (DisabledException e) {
            model.addAttribute("errorMessage", "Tài khoản của bạn đã bị khóa!");
            model.addAttribute("loginDTO", dto);
            return "auth/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Đăng nhập thất bại. Vui lòng thử lại.");
            model.addAttribute("loginDTO", dto);
            return "auth/login";
        }
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("roles", List.of(User.Role.STUDENT, User.Role.LECTURER));
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerDTO") RegisterDTO dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("roles", List.of(User.Role.STUDENT, User.Role.LECTURER));
            return "auth/register";
        }

        try {
            authService.register(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/auth/login";

        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("roles", List.of(User.Role.STUDENT, User.Role.LECTURER));
            return "auth/register";
        }
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "auth/access-denied";
    }
}