package com.sicnelleapplicazioni.servlet;

import com.sicnelleapplicazioni.model.Content;
import com.sicnelleapplicazioni.repository.ContentRepository;
import com.sicnelleapplicazioni.repository.JdbcContentRepository;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

import java.util.stream.Collectors;

public class HomeServlet extends HttpServlet {

    private ContentRepository contentRepository;

    @Override
    public void init() throws ServletException {
        this.contentRepository = new JdbcContentRepository();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        Long userId = (Long) session.getAttribute("userId");
        List<Content> allContents = contentRepository.findAll();

        List<Content> userContents = allContents.stream()
                .filter(c -> c.getUserId().equals(userId))
                .collect(Collectors.toList());

        List<Content> otherUsersContents = allContents.stream()
                .filter(c -> !c.getUserId().equals(userId))
                .collect(Collectors.toList());

        req.setAttribute("userContents", userContents);
        req.setAttribute("otherUsersContents", otherUsersContents);
        req.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(req, resp);
    }
}

