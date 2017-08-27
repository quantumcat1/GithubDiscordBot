package stuff;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.kohsuke.github.GHRepository;

@ManagedBean
@ApplicationScoped
public class Master implements Serializable {
	private static final long serialVersionUID = -9082917625309737085L;
	private CatBot catbot;
	private String token;
	private String password;

	@PostConstruct
	public void initialise() {
		setToken("");
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try (InputStream input = classLoader.getResourceAsStream("config.properties")) {
			Properties prop = new Properties();
			prop.load(input);

			setPassword(prop.getProperty("password"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public CatBot getCatbot() {
		return catbot;
	}

	public String getText() {
		if (catbot == null)
			return "Start Catbot";
		return "Restart Catbot";
	}

	public void changeToken(String token) {
		this.setToken(token);
	}

	public void startCatbot() {
		Map<String, RepoTS> requests = null;
		String superUser = null;
		List<String> authorisedUsers = null;
		Map<String, GHRepository> shortcuts = null;
		String gtoken = "";
		if (catbot != null) {
			requests = catbot.requests;
			superUser = catbot.superUser;
			authorisedUsers = catbot.authorisedUsers;
			shortcuts = catbot.shortcuts;
			if (token.equals("")) {
				gtoken = catbot.githubToken;
			} else {
				gtoken = token;
			}

		}
		if (catbot != null)
			catbot.disconnect();
		catbot = new CatBot(gtoken, requests, superUser, authorisedUsers, shortcuts);
	}

	public boolean checkPassword(String password) {
		return password.equals(this.password);
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
