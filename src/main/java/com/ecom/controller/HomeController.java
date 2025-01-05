package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.UserDtls;
import com.ecom.service.CategoryService;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CommonUtil commonUtil; 
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@ModelAttribute
	public void getUserDetails(Principal p, Model m) {
		
		if(p != null) {
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user", userDtls);
		}
		
		List<Category> getAllCategory = categoryService.getAllActiveCategory();
		m.addAttribute("category", getAllCategory);
	}
	
	
	@GetMapping("/")
	public String index() {
		return "index";
	}
	
	
	@GetMapping("/signin")
	public String login()
	{
		return "login";
	}
	
	
	@GetMapping("/register")
	public String register()
	{
		return "register";
	}
	
	
	@GetMapping("/products")
	public String product(Model m, @RequestParam(value = "category", defaultValue = "") String category)
	{
		List<Category> categories = categoryService.getAllActiveCategory();
		List<Product> products = productService.getAllActiveProduct(category);
		m.addAttribute("categories", categories);
		m.addAttribute("products", products);
		m.addAttribute("paramValue", category);
		
		return "product";
		
	}
	
	@GetMapping("/viewProduct/{id}")
	public String viewProduct(@PathVariable int id, Model m)
	{
		Product getById =  productService.getProductById(id);
		m.addAttribute("product", getById);
		
		return "viewProduct";
		
	}
	
	@PostMapping("/saveUser")
	public String saveUserDtls(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, HttpSession session) throws IOException {
		
		String imageName = file.isEmpty()? "default.jpg" : file.getOriginalFilename();	
		user.setProfileImage(imageName);
		
		UserDtls saveUser =  userService.saveUserDtls(user);
		
		if(!ObjectUtils.isEmpty(saveUser)) {
			
			if(!file.isEmpty()) {
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "Profile_img" + File.separator + file.getOriginalFilename());
				//System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
			}
			session.setAttribute("successMessage", "saved successfully!");
		
		}else {
			session.setAttribute("errorMessage", "Internal server error");
		}
		
		return "redirect:/register";
		
	}
	
	// Forget password code
	@GetMapping("/forget-password")
	public String showForgetPassword() {
		return "forget_password.html";
	}
	
	@PostMapping("/forget-password")
	public String processForgetPassword(@RequestParam String email, HttpSession session, HttpServletRequest request) throws UnsupportedEncodingException, MessagingException {
		
		UserDtls userByEmail = userService.getUserByEmail(email);
		
		if(ObjectUtils.isEmpty(userByEmail)) {
			session.setAttribute("errorMessage", "Invalid Email!");
		}else {
			
			String resetToken = UUID.randomUUID().toString();
			userService.updateUserResetToken(email, resetToken);
			
			//Generate URL http://localhost:8080/reset-password?token=sdjfsffjh
			String url = CommonUtil.generateUrl(request)+"/reset-password?token="+resetToken;
			
			Boolean sendMail = commonUtil.sendMail(url,email);
			if(sendMail) {
				session.setAttribute("successMessage", "please check your mail, password reset link sent");
			}else {
				session.setAttribute("errorMessage", "something wrong on the server!! mail not sent");
			}
		}
		
		return "redirect:/forget-password";
	}
	
	@GetMapping("/reset-password")
	public String showResetPassword(@RequestParam String token, HttpSession session, Model m) {
		UserDtls userByToken = userService.getUserByToken(token);
		
		if(userByToken==null) {
			m.addAttribute("msg", "your link is invalid or expired");
			return "message";
		}
		m.addAttribute("token", token);
		
		return "reset_password";
	}
	
	@PostMapping("/reset-password")
	public String resetPassword(@RequestParam String token, @RequestParam String password, HttpSession session, Model m) {
		UserDtls userByToken = userService.getUserByToken(token);
		
		if(userByToken == null) {
			m.addAttribute("msg", "your link is invalid or expired");
			return "message";
		}else {
			userByToken.setPassword(passwordEncoder.encode(password));
			userByToken.setResetToken(null);
			userService.updateUser(userByToken);
			session.setAttribute("successMessage", "Password changed succcessfully!");
			m.addAttribute("msg", "Password Changed successfully!");
			
			return "message";
		}
	}
		
}
