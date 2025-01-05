package com.ecom.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Product;
import com.ecom.repository.ProductRepository;
import com.ecom.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService{
	
	@Autowired
	private ProductRepository productRepository;

	@Override
	public Product saveProduct(Product product) {
		return productRepository.save(product);
	}

	@Override
	public List<Product> getAllProduct() {
		return productRepository.findAll();
	}

	@Override
	public Boolean deleteProduct(int id) {
		Product product = productRepository.findById(id).orElse(null);
		
		if(!ObjectUtils.isEmpty(product)) {
			productRepository.delete(product);
			return true;
		}else {
			return false;
		}
	}

	@Override
	public Product getProductById(int id) {
		Product product = productRepository.findById(id).orElse(null);
		
		return product;
	}

	@Override
	public Product updateProduct(Product product, MultipartFile img) {
		
		Product oldProduct = getProductById(product.getId());
		String imageName = img.isEmpty()? oldProduct.getImageName(): img.getOriginalFilename();
		
		oldProduct.setImageName(imageName);
		oldProduct.setTitle(product.getTitle());
		oldProduct.setDescription(product.getDescription());
		oldProduct.setCategory(product.getCategory());
		oldProduct.setPrice(product.getPrice());
		oldProduct.setStock(product.getStock());
		oldProduct.setDiscount(product.getDiscount());	
		oldProduct.setIsActive(product.getIsActive());
		
		Double discount = product.getDiscount()*(product.getPrice()/100.0);
		Double afterDiscountPrice = product.getPrice()-discount;
		oldProduct.setDiscountPrice(afterDiscountPrice);
		Product updateProduct = saveProduct(oldProduct);   //productRepository.save(oldProduct);
		
		if(!ObjectUtils.isEmpty(updateProduct)) {
			if(!img.isEmpty()) {
				try {
					File saveFile = new ClassPathResource("static/img").getFile();
					Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "Product_img" + File.separator + img.getOriginalFilename());
					Files.copy(img.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return product;
		}
		return null;
	}

	@Override
	public List<Product> getAllActiveProduct(String category) {
		List<Product> products = null;
		if(ObjectUtils.isEmpty(category)){
			products = productRepository.findByIsActiveTrue();
		}else {
			products = productRepository.findByCategory(category);
		}
		
		return products;
	}
	}
