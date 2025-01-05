package com.ecom.util;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CommonUtil {
	
	@Autowired
	private JavaMailSender mailSender;
	
	public Boolean sendMail(String url, String recipentEmail) throws UnsupportedEncodingException, MessagingException {
		MimeMessage mimeMessage = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
		
		helper.setFrom("prabinbista2057@gmail.com", "Shopping Card");
		helper.setTo(recipentEmail);
		
		String content = "<p>Hello,</p>" + "<p>You have requested to your password.</p>"
						+ "<p>Click the link below to change your password:</p>"
						+ "<p><a href=\"" + url + "\"> Change my passwprd</a></p>";
		helper.setSubject("Password Reset");
		helper.setText(content, true);
		mailSender.send(mimeMessage);
		
		
		
		return true;
	}

	public static String generateUrl(HttpServletRequest request) {
		//http://localhost:8080/forget-password
		String siteUrl = request.getRequestURL().toString();
		
		return siteUrl.replace(request.getServletPath(), "");
	}
}
