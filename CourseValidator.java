package uk.ac.napier.soc.ssd.coursework.domain.validators;

import org.owasp.appsensor.core.*;
import org.owasp.appsensor.core.event.EventManager;
import org.owasp.esapi.ESAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.ac.napier.soc.ssd.coursework.domain.Course;


public class CourseValidator implements Validator {

    private SpringValidatorAdapter validator;

    @Autowired
    private javax.validation.Validator jsr303Validator;

    //@Autowired
    //private RestEventManager eventManager;

    @Override
    public boolean supports(Class<?> clazz) {
        return Course.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        Course course = (Course) target;

        if (course.getDescription() != null && course.getDescription().contains("<script>")
            || course.getDescription() != null && course.getDescription().contains("<img")) {
            signalXss();
            errors.rejectValue("description", "xss.attempt", "You tried XSS - stop!");
        }

    }

    private void signalXss() {
        User user = new User(getUserName());
        // AE3: High Rate of Login Attempts
        DetectionPoint detectionPoint = new DetectionPoint(DetectionPoint.Category.INPUT_VALIDATION, "IE1");
        //eventManager = new RestEventManager();
        //eventManager.addEvent(new Event(user, detectionPoint, getDetectionSystem()));
    }

    private String getUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        // overwrite if we can be more specific
        if (authentication instanceof UserDetails) {
            UserDetails userDetails = (UserDetails)authentication;

            userName = userDetails.getUsername();
        }

        return userName;
    }

    private DetectionSystem getDetectionSystem() {
        return new DetectionSystem("myclientapp");
    }
}
