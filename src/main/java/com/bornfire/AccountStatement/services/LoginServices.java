package com.bornfire.AccountStatement.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
	import org.springframework.web.client.RestTemplate;

import com.bornfire.AccountStatement.entities.UserProfile;
import com.bornfire.AccountStatement.entities.UserProfileRep;
import com.bornfire.AccountStatement.config.PasswordEncryption;
import com.bornfire.AccountStatement.entities.AccountStatementSession;
import com.bornfire.AccountStatement.entities.AuditServicesRep;
import com.bornfire.AccountStatement.entities.Smsserviceotp;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

@Service
@ConfigurationProperties("output")
@Transactional
public class LoginServices {

	private static final Logger logger = LoggerFactory.getLogger(LoginServices.class);
	
	@Autowired
	AuditServicesRep AuditServicesRep;
	
	@Autowired
	UserProfileRep userProfileRep;

	@Autowired
	SessionFactory sessionFactory;
	
	@Autowired
	DataSource srcdataSource;
	
	@Autowired
	RestTemplate restTemplate;

	
	@NotNull
	private String exportpath;
	
	

	@Value("${default.password}")
	private String password;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getExportpath() {
		return exportpath;
	}

	public void setExportpath(String exportpath) {
		this.exportpath = exportpath;
	}

	/*
	 * Getting 3 inputs -
	 * 
	 * UserProfile Object, Formmode - Valid values : add, edit, inputuser - user who
	 * edited the data
	 * 
	 * if formmode is add - Get password from application.properties create the user
	 * 
	 * if formmode is edit - Get password from database for that user and use other
	 * fields came from front end.
	 * 
	 * 
	 */

	public String deleteuser(String userid) {
		String msg = "";
		
		try {
			Optional<UserProfile> user = userProfileRep.findById(userid);
			if(user.isPresent()) {
				userProfileRep.deleteById(userid);
				msg = "User Id Rejected";
				
			}else {
				msg = "Invalid Data";
			}
			
		}catch (Exception e) {
			msg = "Please contact Administrator";
			// TODO: handle exception
		}
		return msg;
	}
	
	
	
	public String addUser(UserProfile userProfile, String formmode, String inputUser,String username,String mob,String role) {

		String msg = "";

		try {

			if (formmode.equals("add")) {
				UserProfile up = userProfile;
				try {
					String encryptedPassword = PasswordEncryption.getEncryptedPassword(userProfile.getPassword());

					if (up.getLogin_status().equals("Active")) {
						up.setUser_locked_flg("N");
					} else {
						up.setUser_locked_flg("Y");
					}

					if (up.getUser_status().equals("Active")) {
						up.setDisable_flg("N");
					} else {
						up.setDisable_flg("Y");
					}

					up.setEntity_flg("N");
					up.setEntry_time(new Date());
					up.setEntry_user(inputUser);
				    up.setAcct_access_code(up.getAcct_access_code());
					up.setLogin_flg("N");//To prompt the user for changing the password at first login
					up.setNo_of_attmp(0);
					up.setLog_in_count("0");
					up.setEmp_name(up.getUsername());
					String localdateval = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
					LocalDate date = LocalDate.parse(localdateval);
					BigDecimal passexpdays = new BigDecimal(up.getPass_exp_days());
					LocalDate date2 = date.plusDays(passexpdays.intValue());
					up.setPass_exp_date(new SimpleDateFormat("yyyy-MM-dd").parse(date2.toString()));
					up.setPassword(encryptedPassword);

					userProfileRep.save(up);

					
					
					/*
					 * //save datas for audit services BigDecimal mobile = new BigDecimal(mob); Date
					 * currentDate = new Date(); Session hs = sessionFactory.getCurrentSession();
					 * System.out.println("first"); BigDecimal srlno = (BigDecimal)
					 * hs.createNativeQuery("SELECT USER_AUDIT_SRL_NO.NEXTVAL AS SRL_NO FROM DUAL")
					 * .getSingleResult(); System.out.println(srlno+"second"); AuditServicesEntity
					 * as=new AuditServicesEntity();
					 * 
					 * as.setSrl_no(srlno); as.setLog_in_user_id(inputUser);
					 * as.setLog_in_user_name(username); as.setLog_in_contact_no(mobile);
					 * as.setLog_in_designation(role); as.setNew_user_id(userProfile.getUserid());
					 * as.setNew_user_name(userProfile.getUsername());
					 * as.setLog_in_audit_date(currentDate);
					 * 
					 * 
					 * AuditServicesRep.save(as);
					 */
					msg = "User Created Successfully";
				
				} catch (Exception e) {
					msg = "Kindly check the data/Please contact Administrator";
					e.printStackTrace();
				}

				
				
				

			}else {

				Optional<UserProfile> up = userProfileRep.findById(userProfile.getUserid());

				if (up.isPresent()) {

					userProfile.setPassword(up.get().getPassword());

					if (userProfile.getLogin_status().equals("Active")) {
						userProfile.setUser_locked_flg("N");
					} else {
						userProfile.setUser_locked_flg("Y");
					}

					if (userProfile.getUser_status().equals("Active")) {
						userProfile.setDisable_flg("N");
					} else {
						userProfile.setDisable_flg("Y");
					}
					
					if(userProfile.getPass_exp_days().equals(up.get().getPass_exp_days())) {
						userProfile.setPass_exp_date(up.get().getPass_exp_date());
					}else {
						String localdateval = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
						LocalDate date = LocalDate.parse(localdateval);
						BigDecimal passexpdays = new BigDecimal(userProfile.getPass_exp_days());
						LocalDate date2 = date.plusDays(passexpdays.intValue());
						userProfile.setPass_exp_date(new SimpleDateFormat("yyyy-MM-dd").parse(date2.toString()));
					}
					userProfile.setLog_in_count(up.get().getLog_in_count() != null ? up.get().getLog_in_count() : "1");
					userProfile.setEntry_user(up.get().getEntry_user());
					userProfile.setEntry_time(up.get().getEntry_time());
					userProfile.setNo_of_attmp(0);
					userProfile.setEntity_flg("N");
					userProfile.setModify_user(inputUser);
					userProfile.setModify_time(new Date());

					userProfileRep.save(userProfile);
					msg = "User Edited Successfully";
				}else {
					msg = "User Not found to edit";
				}

				

			}
		} catch (Exception e) {
			msg = "Error Occured. Please contact Administrator";
			e.printStackTrace();
			logger.info(e.getMessage());
		}

		return msg;
	}

	
	
	
	
	

	public Iterable<UserProfile> getUsersList(String ROLEIDAC) {
	    Iterable<UserProfile> users = Collections.emptyList(); // Default fallback

	    if (ROLEIDAC.equals("Superadmin")) {
	        users = userProfileRep.findAll();
	    } else if (ROLEIDAC.equals("DCD_ADMIN") || ROLEIDAC.equals("DCD_BRANCH")) {
	        users = userProfileRep.getUsersListByecdd(); // Filtered list for ECDD roles
	    } else if (ROLEIDAC.equals("RBR_ADMIN") || ROLEIDAC.equals("RBR_BRANCH")) {
	        users = userProfileRep.getUsersListByrbr(); // Filtered list for RBR roles
	    } else if (ROLEIDAC.equals("BRC")) {
	        users = userProfileRep.getUsersListBybrc(); // Filtered list for BRC role
	    }

	    return users;
	}

	
	
	public List<UserProfile> getUsersListone(String username) {
		Optional<UserProfile> users = userProfileRep.findById(username);
		System.out.println("The domain id is: "+users.get().getDomain_id());
		String[] dataArray = users.get().getDomain_id().split(",");
		List<String> item=new ArrayList<String>();
		List<UserProfile> itemw=new ArrayList<UserProfile>();
		 for (String ss  : dataArray) {
	            item.add(ss);
				  }
		//The entity object created for storing the separate domain id values
			UserProfile userProfile= new UserProfile(); 
         for(int i=0;i<item.size();i++) {
        	 if(i==0) {
 			userProfile.setUserid(item.get(i));
        	 }
 			else if(i==1) {
 				userProfile.setUsername(item.get(i));
				}
			else if(i==2) {
				userProfile.setBranch_name(item.get(i));
				}
			else if(i==3) {
				userProfile.setBank_name(item.get(i));
				}
			else if(i==4) {
				userProfile.setRole_desc(item.get(i));
				}
			else if(i==5) {
				userProfile.setRole_id(item.get(i));
				}
 			itemw.add(userProfile);
         }
		return itemw;
	}

	public UserProfile getUser(String id) {
		logger.info(id);
		if (userProfileRep.existsById(id)) {
			UserProfile up = userProfileRep.findById(id).get();
			logger.info(up.getEntity_flg());
			return up;
		} else {
			UserProfile UserProfile = new UserProfile();
			UserProfile.setLogin_low("09:00");
			UserProfile.setLogin_high("19:00");
			return  UserProfile;
		}

	};
	
	



	

	public String verifyUser(UserProfile userProfile, String inputUser) {
		String msg = "";

		Optional<UserProfile> up = userProfileRep.findById(userProfile.getUserid());

		try {

			if (up.isPresent()) {

				userProfile.setPassword(up.get().getPassword());
				userProfile.setPass_exp_date(up.get().getPass_exp_date());
				userProfile.setEmp_name(up.get().getEmp_name());
				userProfile.setLog_in_count(up.get().getLog_in_count());
				userProfile.setEntry_user(up.get().getEntry_user());
				userProfile.setEntry_time(up.get().getEntry_time());
				userProfile.setNo_of_attmp(0);
				userProfile.setEntity_flg("Y");
				userProfile.setLogin_flg("N");
				userProfile.setAuth_user(inputUser);
				userProfile.setAuth_time(new Date());

				userProfileRep.save(userProfile);
				msg = "User Verified Successfully";
			}else {
				msg = "User not found";
			}

			
		} catch (Exception e) {
			logger.info(e.getMessage());
			e.printStackTrace();
			msg = "Error Occured. Please contact Administrator";
		}

		return msg;
	}

	public String passwordReset(UserProfile userprofile, String userid) {

		String msg = "";

		try {
			String encryptedPassword = PasswordEncryption.getEncryptedPassword(this.password);

			Optional<UserProfile> up = userProfileRep.findById(userprofile.getUserid());

			if (up.isPresent()) {
				UserProfile user = up.get();
				user.setPassword(encryptedPassword);
				user.setNo_of_attmp(0);
				user.setLogin_flg("N");
				user.setUser_locked_flg("N");
				userProfileRep.save(user);
			}

			msg = "Password Resetted Successfully";

		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {

			e.printStackTrace();

			msg = "Error Occured. Please contact Administrator";
		}

		return msg;
	}
	
	public String DefaultpasswordReset(UserProfile userprofile, String userid) {

		String msg = "";

		try {
			String encryptedPassword = PasswordEncryption.getEncryptedPassword("Welcome@123456");

			Optional<UserProfile> up = userProfileRep.findById(userprofile.getUserid());

			if (up.isPresent()) {
				UserProfile user = up.get();
				user.setPassword(encryptedPassword);
				user.setLog_in_count("1");
				user.setNo_of_attmp(0);
				user.setLogin_flg("N");
				user.setUser_locked_flg("N");
				userProfileRep.save(user);
			}

			msg = "Default Password Resetted Successfully";

		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {

			e.printStackTrace();

			msg = "Error Occured. Please contact Administrator";
		}

		return msg;
	}

	/*
	 * Getting LoginFlg -
	 * 
	 * If loginFlg = 'N' - User should be prompted to change password. else thats
	 * not required.
	 * 
	 * Loginflg ='N' will be updated at the time of new user creation and at the
	 * time of password reset by admin.
	 * 
	 */
	public String checkPasswordChangeReq(String userid) {

		Optional<UserProfile> up = userProfileRep.findById(userid);
		String loginflg = up.get().getLogin_flg();

		return loginflg;
	}

	public int checkAcctexpirty(String userid) {

		Optional<UserProfile> up = userProfileRep.findById(userid);
		Date expDate = up.get().getAcc_exp_date();
		Date currDate = new Date();

		DateTime dt1 = new DateTime(currDate);
		DateTime dt2 = new DateTime(expDate);

		int remaindays = Days.daysBetween(dt1, dt2).getDays();

		logger.info("Account Expired in:" + remaindays);
		return remaindays;
	}

	public int checkpassexpirty(String userid) {

		Optional<UserProfile> up = userProfileRep.findById(userid);
		Date expDate = up.get().getPass_exp_date();
		Date currDate = new Date();

		DateTime dt1 = new DateTime(currDate);
		DateTime dt2 = new DateTime(expDate);

		int remaindays = Days.daysBetween(dt1, dt2).getDays();

		logger.info("Password Expired in:" + remaindays);
		return remaindays;
	}

	public String changePassword(String oldpass, String newpass, String userid) {
		String msg = "";

		Optional<UserProfile> up = userProfileRep.findById(userid);

		try {
			if (up.isPresent()) {
				UserProfile user = up.get();
				if (PasswordEncryption.validatePassword(oldpass, user.getPassword())) {
					
					if (!PasswordEncryption.validatePassword(newpass, user.getPassword())) {
						
						String encryptedPassword = PasswordEncryption.getEncryptedPassword(newpass);
						user.setPassword(encryptedPassword);
						user.setLogin_flg("Y");
						
						LocalDateTime localDateTime = user.getPass_exp_date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
						user.setPass_exp_date(Date.from(localDateTime.plusDays(365).atZone(ZoneId.systemDefault()).toInstant()));
						
						userProfileRep.save(user);
						msg = "Password Changed Successfully";
						
					}else {
						
						msg = "New password cannot be Same as Old password";
					}
					
					
				} else {
					msg = "Incorrect Old Password!";
				}
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
			msg = "Error Occured. Please contact Administrator";
		}
		logger.info(msg);
		return msg;
	};

	public void SessionLogging(String menuname, String menuid, String sessionid, String userid, String ip,
			String status) {
		Session hs = sessionFactory.getCurrentSession();

		try {

			if (menuname.equals("LOGOUT")) {

				hs.createQuery("update XBRLSession set status='IN-ACTIVE' where session_id = ?1")
						.setParameter(1, sessionid).executeUpdate();

			} else {
				
				hs.save(new AccountStatementSession(menuname, menuid, sessionid, userid, ip, new Date(), status));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	

	public File getUserLogFile(Date fromdate, Date todate) {
		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

		String path = exportpath;
		String fileName = "USER_LOGS_"+dateFormat.format(new Date())+".xlsx";
		File outputFile;

		File jasperFile;
		
		File folders = new File(path);
		if (!folders.exists()) {
			folders.mkdirs();
		}
		
		
		try {
			jasperFile = ResourceUtils.getFile("classpath:static/jasper/USER_LOGS/UserLogs.jasper");
			JasperReport jr = (JasperReport) JRLoader.loadObject(jasperFile);
			HashMap<String, Object> map = new HashMap<String, Object>();

			logger.info("Assigning Parameters for Jasper");
			map.put("FromDate", dateFormat.format(fromdate));
			map.put("ToDate", dateFormat.format(todate));
			
			
			
			path = path + "/" + fileName;
			JasperPrint jp = JasperFillManager.fillReport(jr, map, srcdataSource.getConnection());
			JRXlsxExporter exporter = new JRXlsxExporter();
			exporter.setExporterInput(new SimpleExporterInput(jp));
			exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(path));
			exporter.exportReport();
			logger.info("Excel File exported");
			
		} catch (FileNotFoundException|JRException|SQLException e) {
			
			e.printStackTrace();
		}


		outputFile = new File(path);	

		
	return outputFile;
	}

	public List<AccountStatementSession> getUserLog(Date fromdate, Date todate) {
		
		
		Session hs = sessionFactory.getCurrentSession();
		
		List<AccountStatementSession> ls = hs.createQuery("from XBRLSession where trunc(entry_time,'DD') between ?1 and ?2 and menu in ('LOGIN','LOGOUT') order by entry_time desc ", AccountStatementSession.class)
		.setParameter(1, fromdate)
		.setParameter(2, todate)
		.getResultList();
		
		
		return ls;
	}
	
	
	public String sendclientotp(String otp,String Roletype,UserProfile UserProfile) {
		
		logger.info("Start Sending OTP");
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		
		Smsserviceotp Smsserviceotp = new Smsserviceotp();
		Smsserviceotp.setWrapperApiKey("LA6m0");
		Smsserviceotp.setSmssenderid("BOBAlert");
		Smsserviceotp.setSmsmobilenumber(UserProfile.getMob_number());
		Smsserviceotp.setSmstext("BRBS Login OTP: "+otp+" . Please do not share this with anyone. Valid for one-time use only.");
		Smsserviceotp.setToemail(UserProfile.getEmail_id());
		Smsserviceotp.setEmailsubject("ECDD OTP");
		Smsserviceotp.setEmailtemplateid("BOBAlert");
		Smsserviceotp.setEmailtext("BRBS Login OTP: "+otp+" . Please do not share this with anyone. Valid for one-time use only.");
		HttpEntity<Smsserviceotp> entity = new HttpEntity<>(Smsserviceotp, httpHeaders);
		
	//	logger.info(entity.toString());
		ResponseEntity<String> response = null;
		try {
            logger.info("Ready to Call URL for OTP");
			response = restTemplate.postForEntity(
					"https://wrap.smshub.live/api/APIWrapper", entity, String.class);
			logger.info(response.toString());

			if (response.getStatusCode() == HttpStatus.OK) {
				logger.info("Send Successfully");
				return "Otpsendsuccessfully";
			} else {
				logger.info("Send Failed");
				return "Something went wrong at server end";
			}

		} catch (HttpClientErrorException ex) {
			if (ex.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
				logger.info("Exception -"+ex.getMessage());
				return "Something went wrong at server end";

			} else {
				logger.info("Exception -"+ex.getMessage());
				return "Something went wrong at server end";
			}

		} catch (HttpServerErrorException ex) {
			logger.info("Exception -"+ex.getMessage());
			return "Something went wrong at server end";
		}
		
	}
		
	
}

