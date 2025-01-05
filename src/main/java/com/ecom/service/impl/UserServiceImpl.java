package com.ecom.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecom.model.UserDtls;
import com.ecom.repository.UserRepository;
import com.ecom.service.UserService;
import com.ecom.util.AppConstant;

@Service
public class UserServiceImpl implements UserService{
	
	
	 @Autowired 
	 private UserRepository userRepository;
	 
	 @Autowired 
	 private PasswordEncoder passwordEncoder;

	@Override
	public UserDtls saveUserDtls(UserDtls user) {
		
		user.setRole("ROLE_USER");
		user.setIsEnable(true);
		String encodePassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodePassword);
		user.setAccountNonLocked(true);
		user.setFailedAttmp(0);
		UserDtls savceUser =  userRepository.save(user);
		return savceUser;
	}

	@Override
	public UserDtls getUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public List<UserDtls> getUsers(String role) {
		return userRepository.findByRole(role);
	}

	@Override
	public Boolean updateAccountStatus(Integer id, Boolean status) {
		
		Optional<UserDtls> findByUser = userRepository.findById(id);
		
		if(findByUser.isPresent()) {
			UserDtls userDtls = findByUser.get();
			userDtls.setIsEnable(status);
			userRepository.save(userDtls);
			return true;
			
		}
		
		return false;
		
	}

	@Override
	public void increaseFailedAttempt(UserDtls user) {
		int attemp = user.getFailedAttmp() + 1;
		user.setFailedAttmp(attemp);
		userRepository.save(user);
	}

	@Override
	public void userAccountLock(UserDtls user) {
		user.setAccountNonLocked(false);
		user.setLockTime(new Date());
		user.setIsEnable(false);
		userRepository.save(user);
	}

	@Override
	public Boolean unlockAccountTimeExpired(UserDtls user) {
		long lockTime = user.getLockTime().getTime();
		long unlockTime = lockTime + AppConstant.UNLOCK_DURATION_TIME;
		
		long currentTime = System.currentTimeMillis();
		if(unlockTime<currentTime) {
			user.setAccountNonLocked(true);
			user.setFailedAttmp(0);
			user.setLockTime(null);
			userRepository.save(user);
			return true;
		}
		return false;
	}

	@Override
	public void resetAttempt(int userId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateUserResetToken(String email, String resetToken) {
		UserDtls findByEmail = userRepository.findByEmail(email);
		findByEmail.setResetToken(resetToken);
		userRepository.save(findByEmail);
		
	}

	@Override
	public UserDtls getUserByToken(String token) {
		
		return userRepository.findByResetToken(token);
		
	}

	@Override
	public UserDtls updateUser(UserDtls user) {
		return userRepository.save(user);
		
	}
	
	

}
