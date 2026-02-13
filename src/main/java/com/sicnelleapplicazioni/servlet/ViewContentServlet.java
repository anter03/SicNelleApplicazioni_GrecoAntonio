package com.sicnelleapplicazioni.servlet;

import com.sicnelleapplicazioni.model.Content;
import com.sicnelleapplicazioni.repository.ContentRepository;
import com.sicnelleapplicazioni.repository.JdbcContentRepository;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ViewContentServlet extends HttpServlet {

	private static final Logger LOGGER = Logger.getLogger(ViewContentServlet.class.getName());
	private ContentRepository contentRepository;

	@Override
	public void init() throws ServletException {
		this.contentRepository = new JdbcContentRepository();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String idParam = req.getParameter("id");
		if (idParam == null || idParam.isEmpty()) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing content ID.");
			return;
		}

		UUID contentId;
		try {
			contentId = UUID.fromString(idParam);
		} catch (IllegalArgumentException e) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid content ID format.");
			return;
		}

		Optional<Content> contentOpt = contentRepository.findById(contentId);

		if (contentOpt.isPresent()) {
			Content content = contentOpt.get();
			try {
				Path filePath = Paths.get(content.getFilePath());
				if (Files.exists(filePath)) {
					String contentText = Files.readString(filePath, StandardCharsets.UTF_8);
					req.setAttribute("content", content);
					req.setAttribute("contentText", contentText);
					req.getRequestDispatcher("/WEB-INF/views/viewContent.jsp").forward(req, resp);
				} else {
					LOGGER.log(Level.WARNING, "File not found for content ID: " + contentId);
					resp.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found.");
				}
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "Error reading file for content ID: " + contentId, e);
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error reading file.");
			}
		} else {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Content not found.");
		}
	}
}
