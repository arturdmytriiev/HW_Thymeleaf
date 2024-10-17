package org.example.controller;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@WebServlet("/time")
public class IndexServlet extends HttpServlet {
    private TemplateEngine engine;
    private String time;
    private Cookie lastTimezone;
    //Пишем для настройки шаблона и поиска шаблонов в папке
    @Override
    public void init() throws ServletException {
        engine = new TemplateEngine();

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        resolver.setCharacterEncoding("UTF-8");
        engine.setTemplateResolver(resolver);
    }
    ////////////////////////////////////

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String timezoneParam = req.getParameter("timezone");
        ZoneId zoneId = null;
        Context context = new Context();
        if(timezoneParam != null &&!timezoneParam.isEmpty())
        {
            if (timezoneParam.contains(" ")) {
                timezoneParam = timezoneParam.replace(" ", "+");
            }
            try {
                zoneId = ZoneId.of(timezoneParam);
                lastTimezone = new Cookie("timezone", timezoneParam);
                lastTimezone.setMaxAge(24*60*60);
                resp.addCookie(lastTimezone);
            }catch (Exception e) {
                zoneId = ZoneId.of("UTC");
            }
        }
        else{
            Cookie[] cookies = req.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("timezone")) {
                        timezoneParam = cookie.getValue();
                        zoneId = ZoneId.of(timezoneParam);
                        break;
                    }
                }
            }
        }

        if(zoneId == null) {
            zoneId = ZoneId.of("UTC");
        }

        time = formatTimeZone(zoneId);
        context.setVariable("time", time);
        resp.setContentType("text/html; charset=UTF-8");
        engine.process("index", context, resp.getWriter());
    }

    private String formatTimeZone(ZoneId zoneId ){
        ZonedDateTime dateTime = ZonedDateTime.now(zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String time = dateTime.format(formatter)+" "+ zoneId;
        return time;
    }


}
