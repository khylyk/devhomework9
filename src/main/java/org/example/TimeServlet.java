package org.example;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


@WebServlet(value = "/timew")
public class TimeServlet extends HttpServlet {
    private TemplateEngine engine;

    @Override
    public void init() throws ServletException{
        engine = new TemplateEngine();

        ServletContext servletContext = this.getServletContext();
        String templatePath = servletContext.getRealPath("/WEB-INF/classes/templates/");

        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setPrefix(templatePath);
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        resolver.setCharacterEncoding("UTF-8");
        engine.addTemplateResolver(resolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String timezoneParam = req.getParameter("timezone");
        String lastTimezone = getLastTimezoneFromCookie(req);

        ZoneId zoneId;

        if (timezoneParam != null && !timezoneParam.isEmpty()) {
            zoneId = ZoneId.of(timezoneParam);
            saveLastTimezoneToCookie(resp, timezoneParam);
        } else if (lastTimezone != null) {
            zoneId = ZoneId.of(lastTimezone);
        } else {
            zoneId = ZoneId.of("UTC");
        }

        LocalDateTime localDateTime = LocalDateTime.now();
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(zoneId);
        String formattedTime = zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'xxx"));

        Context context = new Context(req.getLocale());
        context.setVariable("currentTime", formattedTime);
        context.setVariable("timezone", zoneId.getId());

        engine.process("time_template", context, resp.getWriter());
    }

    private String getLastTimezoneFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("lastTimezone".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void saveLastTimezoneToCookie(HttpServletResponse response, String timezone) {
        Cookie cookie = new Cookie("lastTimezone", timezone);
        cookie.setMaxAge(30 * 24 * 60 * 60); // Cookie expiration time in seconds (30 days)
        response.addCookie(cookie);
    }

}