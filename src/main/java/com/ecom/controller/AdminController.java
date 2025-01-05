package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.UserDtls;
import com.ecom.service.CategoryService;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private UserService userService;
	

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
		return "admin/index";
		
	}
	
	
	@GetMapping("/addProduct")
	public String addProduct(Model m) {
		List<Category> categories = categoryService.getAllCategory();
		m.addAttribute("category", categories);
		
		return "admin/addProduct";
		
	}
	
	@GetMapping("/addCategory")
	public String addCategory(Model m) {
		m.addAttribute("categories", categoryService.getAllCategory());
		
		return "admin/addCategory";
		
	}
	
	
	@PostMapping("/saveCategory")
	public String saveCategory(@ModelAttribute Category category,@RequestParam("file") MultipartFile file , HttpSession session) throws IOException {
		
		String imageName = file != null ? file.getOriginalFilename() : "default.jpg";
		category.setImageName(imageName);
		
		
		Boolean existCategory = categoryService.existCategory(category.getCategoryName());
		if(existCategory) {
			session.setAttribute("errorMessage", "Category name already exisst!");
		}else {
			Category saveCategory = categoryService.saveCategory(category);
			if(ObjectUtils.isEmpty(saveCategory)) {
				session.setAttribute("saveError", "Internal server error so not saved");
			}else {
				
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+"Category_img"+File.separator+file.getOriginalFilename());
				//System.out.println(path);
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				
				session.setAttribute("successMessage", "saved successfully!");
			}
		}
		return "redirect:/admin/addCategory";
	}
	
	@GetMapping("/deleteCategory/{id}")
	public String deleteCategory(@PathVariable int id, HttpSession session) {
		
		Boolean deleteCategory = categoryService.deleteCategory(id);
		if(deleteCategory) {
			session.setAttribute("successMessage", "Category deleted successfully!");
			
		}else {
			session.setAttribute("errorMessage", "Internal serverError or something went wrong!");
		}
		
		return "redirect:/admin/addCategory";
	}
	
	@GetMapping("/editCategory/{id}")
	public String EditCategory(@PathVariable int id, Model m) {
		
		m.addAttribute("category",categoryService.getCategoryById(id));
		
		return "/admin/editCategory";
	}
	
	@PostMapping("/updateCategory")
	public String updatecategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file, HttpSession session) throws IOException {
		
		Category oldCategory = categoryService.getCategoryById(category.getId());
		String imageName = file.isEmpty() ? oldCategory.getImageName():file.getOriginalFilename();
		
		if(!ObjectUtils.isEmpty(category)) {
			oldCategory.setCategoryName(category.getCategoryName());
			oldCategory.setIsActive(category.getIsActive());
			oldCategory.setImageName(imageName);
		}
		
		Category updateCateggory = categoryService.saveCategory(oldCategory);
		if(!ObjectUtils.isEmpty(updateCateggory)) {
			
			if(!file.isEmpty()) {
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+"Category_img"+File.separator+file.getOriginalFilename());
				//System.out.println(path);
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
			}
			
			session.setAttribute("successMessage", "Category uptaded successfully!");
		}else {
			session.setAttribute("errorMessage", "Internal server Error!");
		}
		return "redirect:/admin/editCategory/" + category.getId();
	}
	
	
	@PostMapping("/saveProduct")
	public String saveProduct(@ModelAttribute Product product, HttpSession session, @RequestParam("file") MultipartFile image) throws IOException {
		String imageName = image.isEmpty()?"default.jpg" : image.getOriginalFilename();
		product.setImageName(imageName);
		
		product.setDiscount(0);
		product.setDiscountPrice(product.getPrice());
		Product saveProduct = productService.saveProduct(product);
		if(!ObjectUtils.isEmpty(saveProduct)) {
			
			File saveFile = new ClassPathResource("static/img").getFile();
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+"Product_img"+File.separator+image.getOriginalFilename());
			//System.out.println(path); 
			Files.copy(image.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
	
			session.setAttribute("successMessage", "Product saved successfully!");
			
		}else {
			session.setAttribute("errorMessage", "Internal server Error");
			
		}
		return "redirect:/admin/addProduct";
	}
	
	@GetMapping("/viewProducts")
	public String loadViewProduct(Model m) {
		m.addAttribute("products",productService.getAllProduct());
		
		return"admin/viewProduct";
	}
	@GetMapping("/editProduct/{id}")
	public String editProduct(@PathVariable int id, Model m) {
		m.addAttribute("product", productService.getProductById(id));
		m.addAttribute("category", categoryService.getAllCategory());
		
		return "admin/editProduct";
	}
	
	@GetMapping("/deleteProduct/{id}")
	public String deleteProduct(@PathVariable int id, HttpSession session) {
		Boolean deleteProduct = productService.deleteProduct(id);
		
		if(deleteProduct) {
			session.setAttribute("successMessage", "Product deleted successfully!");
		}else {
			session.setAttribute("errorMessage", "Internal server error for deletion!!"); 
		}
		
		return "redirect:/admin/viewProducts";
	}
	@PostMapping("/updateProduct")
	public String editProduct(@ModelAttribute Product product, @RequestParam("pImg") MultipartFile img, HttpSession session) throws IOException {
		
		if(product.getDiscount()<0||product.getDiscount()>100) {
			session.setAttribute("errorMessage", "Invalid Discount");
		}else {
			Product updateProduct = productService.updateProduct(product,img);
			if(!ObjectUtils.isEmpty(updateProduct)) {
				session.setAttribute("successMessage", "Product updated successfully!");
			}
			session.setAttribute("errorMessage", "Internal server error!");
		}
		return "redirect:/admin/editProduct/" + product.getId();
	}
	
	@GetMapping("/users")
	public String getAllUsers(Model m) {
		
		List<UserDtls> users = userService.getUsers("ROLE_USER");
		m.addAttribute("users", users);
		
		return "/admin/user";
		
	}
	
	@GetMapping("/updateStatus")
	public String updateUserAccountStatus(@RequestParam Integer id, @RequestParam Boolean status, HttpSession session) {
		
		Boolean f = userService.updateAccountStatus(id, status);
		if(f) {
			session.setAttribute("successMessage", "Account Status updated successfully!");
		}else {
			session.setAttribute("errorMessage", "Somthing went wromg!");
		}
		
		return "redirect:/admin/users";
	}
}
