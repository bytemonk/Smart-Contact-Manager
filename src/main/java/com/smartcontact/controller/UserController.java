package com.smartcontact.controller;

import java.security.Principal;

import javax.servlet.http.HttpSession;

import org.apache.catalina.webresources.MyCacheStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.smartcontact.dao.ContactRepository;
import com.smartcontact.dao.UserRepository;
import com.smartcontact.entities.Contact;
import com.smartcontact.entities.User;
import com.smartcontact.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model m, Principal p) {
		
		//Getting  user by username (email)
			String userName = p.getName();
			
			
			User user = userRepository.getUserByUserName(userName);
			
			m.addAttribute("user",user);
		

	}
	
	//home dashboard
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {

		
		return "/user/user_dashboard";
	}
	
	
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model){
		
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		
		return "user/add_contact_form";
	}
	
	
	//processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
				Principal principal, HttpSession session) {
		
		
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
		
		
			user.getContacts().add(contact);
			contact.setUser(user);
			this.userRepository.save(user);
	//		System.out.println("Added to DB");
			
			
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
		return "user/add_contact_form";

	}
	
	
	//show contacts handler
	//per page = 5[n]
	//current page = 0 [page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal) {
		
		m.addAttribute("title","My Contacts");
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		Pageable pageable = PageRequest.of(page, 8);
		
		Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(), pageable);
		
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage",page);
		
		m.addAttribute("totalPages",contacts.getTotalPages());
		
		return "user/show_contacts";
	}

	//showing contact details
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model,Principal principal) {
		
		Contact contact = this.contactRepository.findById(cId).get();
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId()) {
			model.addAttribute("contact",contact);	
			model.addAttribute("title",contact.getName());
		}
		
		
		return "/user/conatct_detail";
	}
	
	//delete contact handler
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId,Principal principal, HttpSession session) {
		
		Contact contact = this.contactRepository.findById(cId).get();
		
		//check
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId()) {
		contact.setUser(null);
		this.contactRepository.delete(contact);
		session.setAttribute("message", new Message("Contact deleted successfully!!","success"));
		}
		
		return "redirect:/user/show-contacts/0";
	}
	
	
	//open update form handler
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cId  ,Model model) {
		
		model.addAttribute("title","Update Contact");
		Contact contact = this.contactRepository.findById(cId).get();
		model.addAttribute("contact",contact);
		
		return "user/update_form";
	}


	//update contact process handler
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact, 
			Principal principal, HttpSession session){
		
		try {
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			
			
			user.getContacts().add(contact);
			contact.setUser(user);
			this.userRepository.save(user);

			System.out.println(contact);
			System.out.println(user);
			session.setAttribute("message", new Message("Contact updated successfully!!","success"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "redirect:/user/"+contact.getcId()+"/contact";
	}


	//My Profile Handler
	@GetMapping("/profile")
	public String myProfile(Model model) {
		
		model.addAttribute("title","Profile");
		return "user/profile";
	}




}
