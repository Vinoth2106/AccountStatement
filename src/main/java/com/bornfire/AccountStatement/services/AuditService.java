package com.bornfire.AccountStatement.services;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import javax.servlet.http.HttpServletRequest;
import javax.persistence.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.bornfire.AccountStatement.entities.AuditServicesEntity;
import com.bornfire.AccountStatement.entities.AuditServicesRep;
import com.bornfire.AccountStatement.entities.Service_audit_table_Rep;
import com.bornfire.AccountStatement.entities.Service_audit_table_entity;
import com.bornfire.AccountStatement.entities.UserProfile;

@Service
public class AuditService {

	@Autowired
	private AuditServicesRep auditServicesRep;

	@Autowired
	Service_audit_table_Rep Service_audit_table_Reps;

	public void createLoginAudit(final String customerId, final String functionCode, final String screenName,
			final Map<String, String> changeDetails, final String tableName, final UserProfile user) {
		try {
			final UUID auditID = UUID.randomUUID();
			ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			String userId = null;
			String username = null;
			if (attr != null) {
				HttpServletRequest request = attr.getRequest();
				userId = (String) request.getSession().getAttribute("USERID");
				username = (String) request.getSession().getAttribute("USERNAME");
			}
			final Date currentDate = new Date();

			AuditServicesEntity audit = new AuditServicesEntity();
			audit.setAudit_ref_no(auditID.toString());
			audit.setAudit_date(currentDate);
			audit.setEntry_time(currentDate);
			audit.setEntry_user(user.getUserid());
			audit.setEntry_user_name(user.getUsername());
			audit.setFunc_code(functionCode);
			audit.setRemarks(functionCode+" Successfully ");
			audit.setAudit_table(tableName);
			audit.setAudit_screen(screenName);
			audit.setEvent_id(user.getUserid());
			audit.setEvent_name(user.getUsername());

			if (changeDetails != null && !changeDetails.isEmpty()) {
				StringBuilder changes = new StringBuilder();
				changeDetails
						.forEach((field, value) -> changes.append(field).append(": ").append(value).append("||| "));

				audit.setChange_details(changes.toString());
			}

			if ("VERIFY".equalsIgnoreCase(functionCode)) {
				audit.setAuth_user(userId);
				audit.setAuth_user_name(username);
				audit.setAuth_time(currentDate);
			}

			audit.setReport_id(customerId);

//			System.out.println(audit);
			auditServicesRep.save(audit);

		} catch (Exception e) {
			System.err.println("Error creating business audit: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void createBusinessAudit(final String customerId, final String functionCode, final String screenName,
			final Map<String, String> changeDetails, final String tableName) {
		try {
			System.out.println("Entered Service Audit");
			final UUID auditID = UUID.randomUUID();
			ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			// System.out.println("ServletRequestAttributes : "+attr);
			String userId = null;
			String username = null;
			if (attr != null) {
				HttpServletRequest request = attr.getRequest();
				userId = (String) request.getSession().getAttribute("USERID");
				username = (String) request.getSession().getAttribute("USERNAME");
			}
			final Date currentDate = new Date();

			Service_audit_table_entity audit = new Service_audit_table_entity();
			audit.setAudit_ref_no(auditID.toString());
			audit.setAudit_date(currentDate);
			audit.setEntry_time(currentDate);
			audit.setEntry_user(userId);
			audit.setEntry_user_name(username);
			audit.setFunc_code(functionCode);
			audit.setAudit_table(tableName);
			audit.setAudit_screen(screenName);
			audit.setEvent_id(userId);
			audit.setEvent_name(username);

			if (changeDetails != null && !changeDetails.isEmpty()) {
				StringBuilder changes = new StringBuilder();
				changeDetails
						.forEach((field, value) -> changes.append(field).append(": ").append(value).append("||| "));

				audit.setModi_details(changes.toString());
			}

			if ("VERIFY".equalsIgnoreCase(functionCode)) {
				audit.setAuth_user(userId);
				audit.setAuth_user_name(username);
				audit.setAuth_time(currentDate);
			}

			audit.setDomain_id(customerId);

//			System.out.println(audit);
			Service_audit_table_Reps.save(audit);

		} catch (Exception e) {
			System.err.println("Error creating business audit: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void createBusinessAuditbackground(final String customerId, final String functionCode,
			final String screenName, final Map<String, String> changeDetails, final String tableName,
			ServletRequestAttributes attr) {
		try {
			System.out.println("Entered Service Audit");
			final UUID auditID = UUID.randomUUID();
			// ServletRequestAttributes attr = (ServletRequestAttributes)
			// RequestContextHolder.getRequestAttributes();
			// System.out.println("ServletRequestAttributes : "+attr);
			String userId = null;
			String username = null;
			if (attr != null) {
				HttpServletRequest request = attr.getRequest();
				userId = (String) request.getSession().getAttribute("USERID");
				username = (String) request.getSession().getAttribute("USERNAME");
			}
			final Date currentDate = new Date();

			Service_audit_table_entity audit = new Service_audit_table_entity();
			audit.setAudit_ref_no(auditID.toString());
			audit.setAudit_date(currentDate);
			audit.setEntry_time(currentDate);
			audit.setEntry_user(userId);
			audit.setEntry_user_name(username);
			audit.setFunc_code(functionCode);
			audit.setAudit_table(tableName);
			audit.setAudit_screen(screenName);
			audit.setEvent_id(userId);
			audit.setEvent_name(username);

			if (changeDetails != null && !changeDetails.isEmpty()) {
				StringBuilder changes = new StringBuilder();
				changeDetails
						.forEach((field, value) -> changes.append(field).append(": ").append(value).append("||| "));

				audit.setModi_details(changes.toString());
			}

			if ("VERIFY".equalsIgnoreCase(functionCode)) {
				audit.setAuth_user(userId);
				audit.setAuth_user_name(username);
				audit.setAuth_time(currentDate);
			}

			audit.setDomain_id(customerId);

//			System.out.println(audit);
			Service_audit_table_Reps.save(audit);

		} catch (Exception e) {
			System.err.println("Error creating business audit: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	@PersistenceContext
	EntityManager entityManager;

	public <T> void compareEntities(T oldEntity, T newEntity, String Screenname) {
		System.out.println("Screen Name: " + Screenname);

		if (oldEntity == null || newEntity == null) {
			throw new IllegalArgumentException("Entities cannot be null");
		}

		// Check if both entities are of the same type
		if (!oldEntity.getClass().equals(newEntity.getClass())) {
			throw new IllegalArgumentException(
					"Entities must be of the same type. " + "Old entity type: " + oldEntity.getClass().getSimpleName()
							+ ", New entity type: " + newEntity.getClass().getSimpleName());
		}
		List<String> ignoreFields = Arrays.asList("createdAt", "updatedAt", "modifiedAt", "createdBy", "modifiedBy",
				"lastModifiedDate", "lastModifiedBy", "version");
		List<String> lowerCaseIgnoreFields = new ArrayList<String>();
		if (ignoreFields != null) {
			for (String field : ignoreFields) {
				lowerCaseIgnoreFields.add(field.toLowerCase());
			}
		}

		// Extract and print Primary Key
		EntityType<?> entityType = entityManager.getMetamodel().entity(newEntity.getClass());
		List<String> idNames = new ArrayList<String>();

		if (entityType.hasSingleIdAttribute()) {
			// Single @Id
			for (SingularAttribute<?, ?> attribute : entityType.getSingularAttributes()) {
				if (attribute.isId()) {
					idNames.add(attribute.getName());
					break;
				}
			}
		} else {
			// Composite @IdClass
			for (Object attrObj : entityType.getIdClassAttributes()) {
				SingularAttribute<?, ?> attribute = (SingularAttribute<?, ?>) attrObj;
				idNames.add(attribute.getName());
			}
		}

		// Get the ID value
		Object idValue = entityManager.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(newEntity);

		System.out.println("--- Extracting Composite Key Values ---");
		String formattedIdString = "";

		if (idValue != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

			if (entityType.hasSingleIdAttribute()) {
				// Single Primary Key
				String singleIdName = idNames.get(0);
				String displayName = singleIdName.replace('_', ' ');
				displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);
				String displayValue = (idValue instanceof Date) ? sdf.format((Date) idValue) : idValue.toString();
				formattedIdString = displayName + " : " + displayValue;
			} else {
				// Composite Primary Key
				StringJoiner joiner = new StringJoiner(";");
				Field[] fields = idValue.getClass().getDeclaredFields();

				for (Field field : fields) {
					field.setAccessible(true);
					try {
						String fieldName = field.getName();
						Object fieldValue = field.get(idValue);

						if (!fieldName.equals("serialVersionUID")) {
							String displayName = fieldName.replace('_', ' ');
							displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);
							Object displayValue = (fieldValue instanceof Date) ? sdf.format((Date) fieldValue)
									: fieldValue;
							joiner.add(displayName + " : " + displayValue);
						}
					} catch (IllegalAccessException e) {
						System.err.println("Could not read field: " + field.getName());
					}
				}
				formattedIdString = joiner.toString();
			}
		}

		System.out.println("ID values : " + formattedIdString);

		// Compare entities and build changes string
		StringBuilder changes = new StringBuilder();
		Class<?> clazz = oldEntity.getClass();
		Field[] fields = clazz.getDeclaredFields();

		for (Field field : fields) {
			// Skip if field is in ignore list (case-insensitive)
			if (lowerCaseIgnoreFields.contains(field.getName().toLowerCase())) {
				continue;
			}

			field.setAccessible(true);
			try {
				Object oldValue = field.get(oldEntity);
				Object newValue = field.get(newEntity);

				if (!areEqual(oldValue, newValue)) {
					if (changes.length() > 0) {
						changes.append("|||");
					}
					changes.append(field.getName()).append(": OldValue: ").append(oldValue).append(", NewValue: ")
							.append(newValue);
				}
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Error accessing field: " + field.getName(), e);
			}
		}

		Class<?> entityClass = oldEntity.getClass();
		String tableName = "";
		if (entityClass.isAnnotationPresent(Table.class)) {
		    Table table = entityClass.getAnnotation(Table.class);
		    tableName = table.name();
		    
		    if (tableName != null && !tableName.trim().isEmpty()) {
		        System.out.println("Table name is : "+tableName);
		    }
		}		
	        
		System.out.println("Changes in Audit : " + changes);

		System.out.println("Entered Dynamic  Modify Service Audit");
		final UUID auditID = UUID.randomUUID();
		ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		String userId = null;
		String username = null;
		if (attr != null) {
			HttpServletRequest request = attr.getRequest();
			userId = (String) request.getSession().getAttribute("USERID");
			username = (String) request.getSession().getAttribute("USERNAME");
		}
		final Date currentDate = new Date();

		Service_audit_table_entity audit = new Service_audit_table_entity();
		audit.setAudit_ref_no(auditID.toString());
		audit.setAudit_date(currentDate);
		audit.setEntry_time(currentDate);
		audit.setEntry_user(userId);
		audit.setEntry_user_name(username);
		audit.setFunc_code("MODIFY");
		audit.setAudit_table(tableName);
		audit.setAudit_screen(Screenname);
		audit.setEvent_id(userId);
		audit.setEvent_name(username);
		audit.setModi_details(changes.toString());
		audit.setDomain_id(formattedIdString);

		Service_audit_table_Reps.save(audit);

	}

	private boolean areEqual(Object obj1, Object obj2) {
		if (obj1 == null && obj2 == null)
			return true;
		if (obj1 == null || obj2 == null)
			return false;
		return obj1.equals(obj2);
	}

/*
	public String fetchChanges(@RequestParam(required = false) String audit_ref_no) {

		return auditServicesRep.getchanges(audit_ref_no);
	}
*/
}
