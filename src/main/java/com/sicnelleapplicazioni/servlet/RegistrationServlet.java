import com.sicnelleapplicazioni.repository.JdbcUserRepository;
import com.sicnelleapplicazioni.repository.UserRepository;
import com.sicnelleapplicazioni.service.CaptchaService;
import com.sicnelleapplicazioni.service.EmailService;
import com.sicnelleapplicazioni.service.RegistrationService;
import com.sicnelleapplicazioni.util.ValidationUtil; // Add this import

import java.security.SecureRandom;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/register")
public class RegistrationServlet extends HttpServlet {

    private RegistrationService registrationService;
    private CaptchaService captchaService; // Add CaptchaService instance

    // Public setters for testing purposes
    public void setRegistrationService(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    public void setCaptchaService(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    @Override
    public void init() throws ServletException {
        // Instantiate dependencies
        UserRepository userRepository = new JdbcUserRepository();
        EmailService emailService = new EmailService();
        this.registrationService = new RegistrationService(userRepository, emailService);

        // Instantiate CaptchaService
        this.captchaService = new CaptchaService("YOUR_RECAPTCHA_SECRET_KEY"); // Placeholder secret key
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();

        String username = req.getParameter("username");
        char[] password = req.getParameter("password").toCharArray();

        // Server-side Username Validation
        if (!ValidationUtil.isValidUsername(username)) {
            session.setAttribute("errorMessage", "Invalid username. Username must be alphanumeric and between 3 and 20 characters.");
            resp.sendRedirect(req.getContextPath() + "/register.jsp");
            return;
        }

        // CAPTCHA Validation
        String captchaResponse = req.getParameter("g-recaptcha-response");
        try {
            if (!this.captchaService.validateCaptcha(captchaResponse)) { // Use instance method
                // Log this attempt for monitoring
                System.out.println("CAPTCHA validation failed for user: " + username);
                session.setAttribute("errorMessage", "CAPTCHA verification failed. Please try again.");
                resp.sendRedirect(req.getContextPath() + "/register.jsp");
                return;
            }
        } catch (IOException e) {
            System.err.println("Error while validating CAPTCHA: " + e.getMessage());
            session.setAttribute("errorMessage", "An unexpected error occurred during CAPTCHA validation. Please try again.");
            resp.sendRedirect(req.getContextPath() + "/register.jsp");
            return;
        }

        boolean success = registrationService.register(username, password);

        // Add a random delay to mitigate timing attacks
        try {
            Thread.sleep(new SecureRandom().nextInt(500) + 500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (success) {
            session.setAttribute("successMessage", "Registration successful! Please check your email to verify your account.");
            resp.sendRedirect(req.getContextPath() + "/register.jsp");
        } else {
            session.setAttribute("errorMessage", "Registration failed. Please try again.");
            resp.sendRedirect(req.getContextPath() + "/register.jsp");
        }
    }
}
