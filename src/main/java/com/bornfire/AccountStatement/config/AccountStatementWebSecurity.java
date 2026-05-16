package com.bornfire.AccountStatement.config;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.transaction.annotation.Transactional;


import com.bornfire.AccountStatement.entities.UserAuditRepo;
import com.bornfire.AccountStatement.entities.UserProfile;
import com.bornfire.AccountStatement.entities.UserProfileRep;
import com.bornfire.AccountStatement.services.LoginServices;


@Configuration
@EnableWebSecurity
public class AccountStatementWebSecurity extends WebSecurityConfigurerAdapter {
	
	@Autowired
	UserProfileRep userProfileRep;

	@Autowired
	SessionFactory sessionFactory;

	@Autowired
	LoginServices loginServices;


	@Autowired
	SequenceGenerator sequence;

	
	@Autowired
	UserAuditRepo userAuditRepo;


//	private static final Logger logger = LoggerFactory.getLogger(XBRLReportsController.class);

	private final Integer SESSION_TIMEOUT_IN_SECONDS = 650000;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers("/webjars/**", "/images/**", "/login*","/systemotp", "/verify-otp", "/resetPassword1", "/resetPassword",
						"/freezeColumn/**", "favicon.ico")
				.permitAll().anyRequest().authenticated().and().formLogin().loginPage("/login").permitAll()
				.failureHandler(xbrlAuthFailHandle()).successHandler(xbrlAuthSuccessHandle())
				.usernameParameter("userid").and().logout().permitAll().and().logout()
				.logoutSuccessHandler(xbrlLogoutSuccessHandler()).permitAll().and().sessionManagement()
				.maximumSessions(1).maxSessionsPreventsLogin(false);

		http.csrf().disable();

	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authenticationProvider());
	}

	@Bean

	public AuthenticationProvider authenticationProvider() {

		DaoAuthenticationProvider ap = new DaoAuthenticationProvider() {

			@Override
			@Transactional
			public Authentication authenticate(Authentication authentication) throws AuthenticationException {
				String userid = authentication.getName();
				String password = authentication.getCredentials().toString();

				Optional<UserProfile> up = userProfileRep.findById(userid);

				try {

					if (up.isPresent()) {
						UserProfile usr = up.get();
						if (usr.getLog_in_count().equals("0")) {
							throw new CredentialsExpiredException("Password reset required:" + userid);
						} else if (!usr.isCredentialsNonExpired()) {

							throw new CredentialsExpiredException("Credentials Expired:" + userid);

						} else if (!usr.isAccountNonLocked()) {

							throw new LockedException("Account Locked");

						} else if (!usr.isEnabledUser()) {

							throw new LockedException("Account Disabled");

						} else if (!usr.isverifiedUser()) {

							throw new LockedException("Account Not verified");

						} else if (!PasswordEncryption.validatePassword(password, usr.getPassword())) {

							logger.info("Passing Userid :" + userid);

							Session hs = sessionFactory.getCurrentSession();
							Transaction tr = hs.getTransaction();
							hs.createQuery(
									"update UserProfile a set a.no_of_attmp=nvl(a.no_of_attmp,0)+1, a.user_locked_flg=decode(nvl(a.no_of_attmp,0)+1,'3','Y','N'), a.disable_flg=decode(nvl(a.no_of_attmp,0)+1,'3','Y','N') where userid=?1")
									.setParameter(1, userid).executeUpdate();
							tr.commit();
							hs.close();
							throw new BadCredentialsException("Authentication failed");

						} else {

							return new UsernamePasswordAuthenticationToken(userid, password, Collections.emptyList());

						}

					} else {

						throw new UsernameNotFoundException("Invalid User Name");
					}

				} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
					e.printStackTrace();
					authentication.setAuthenticated(false);
				}
				return authentication;

			}

			@Override
			public boolean supports(Class<?> aClass) {
				return aClass.equals(UsernamePasswordAuthenticationToken.class);
			}

		};

		ap.setHideUserNotFoundExceptions(false);
		ap.setUserDetailsService(userDetailsService());

		return ap;
	}

	@Bean
	@Override
	public UserDetailsService userDetailsService() {

		return new UserDetailsService() {
			@Override
			public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

				Optional<UserProfile> up = userProfileRep.findByusername(username);

				if (up.isPresent()) {
					return up.get();
				} else {
					return new UserProfile();
				}

			}

		};
	}

	@Bean
	public AuthenticationFailureHandler xbrlAuthFailHandle() {
		return new AuthenticationFailureHandler() {

			@Override
			public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
					AuthenticationException exception) throws IOException, ServletException {
				if (exception instanceof CredentialsExpiredException) {
					System.out.println(exception.getMessage().split(":")[1]);
					request.getSession().setAttribute("USERID", exception.getMessage().split(":")[1]);
					request.getSession().setAttribute("PASSWORDERROR", exception.getMessage().split(":")[0]);
					response.sendRedirect("resetPassword1");
				} else {
					response.setStatus(HttpStatus.UNAUTHORIZED.value());

					//logger.info(exception.getMessage());

					response.sendRedirect("login?error=" + exception.getMessage());
				}

			}

		};

	}
	
	
	@Bean
	public AuthenticationSuccessHandler xbrlAuthSuccessHandle() {
		return new AuthenticationSuccessHandler() {

			@Override
			public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
					Authentication authentication) throws IOException, ServletException {
				
				Optional<UserProfile> up = userProfileRep.findById(authentication.getName());
				UserProfile user = up.get();
				user.setNo_of_attmp(0);
				user.setUser_locked_flg("N");
				userProfileRep.save(user);
				
				loginServices.SessionLogging("LOGIN","M1",request.getSession().getId(),user.getUserid(),request.getRemoteAddr(),
						"ACTIVE");
				//String menus = accessRepository.getMenulist(user.getRole_id());
				
//				System.out.println(menus + "MENUS");
//				System.out.println(user.getRole_id() + "ROLEiD");
				
				
			
				request.getSession().setMaxInactiveInterval(SESSION_TIMEOUT_IN_SECONDS);
				request.getSession().setAttribute("USERID", user.getUserid());
				request.getSession().setAttribute("USERNAME", user.getUsername());
				request.getSession().setAttribute("ROLEID", user.getRole_id());
				request.getSession().setAttribute("DOMAINID", user.getDomain_id());
				request.getSession().setAttribute("PERMISSIONS", user.getPermissions());
				request.getSession().setAttribute("WORKCLASS", user.getWork_class());
				request.getSession().setAttribute("MOBILENUMBER", user.getMob_number());
				request.getSession().setAttribute("ROLEDESC", user.getRole_desc());
				request.getSession().setAttribute("ACCESSCODE", user.getAcct_access_code());
				request.getSession().setAttribute("BRANCHCODE", user.getBranch_code());
				request.getSession().setAttribute("BRANCHNAME", user.getBranch_name());
				//request.getSession().setAttribute("MENULIST", menus); 
				//request.getSession().setAttribute("DEPARTMENT",  user.getDepartment());

				//auditService.createLoginAudit(user.getUserid() , "Login",null , null, "XBRL_USER_PROFILE_TABLE",user);				
				
				response.sendRedirect("Dashboard");
				
			}

		};

	}
	
	@Bean
	public LogoutSuccessHandler xbrlLogoutSuccessHandler() {
		
		return new LogoutSuccessHandler() {

			@Override
			public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
					Authentication authentication) throws IOException, ServletException {

				Optional<UserProfile> up = userProfileRep.findById(authentication.getName());
				UserProfile user = up.get();
				//auditService.createLoginAudit(user.getUserid(), "Logout", null, null, "XBRL_USER_PROFILE_TABLE",user);

				response.sendRedirect("login?logout");

			}
		
		
	};

	
	

	}
	
	
	
	
}	
