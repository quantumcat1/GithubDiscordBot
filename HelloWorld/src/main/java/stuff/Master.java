package stuff;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.kohsuke.github.GHRepository;

@ManagedBean
@ApplicationScoped
public class Master implements Serializable
{
	private static final long serialVersionUID = -9082917625309737085L;
	private CatBot catbot;

	@PostConstruct
	public void initialise()
	{
		//catbot = new CatBot();
	}

	public String getHello()
	{
		Map<String, RepoTS> requests = null;
		String superUser = null;
		List<String> authorisedUsers = null;
		Map<String, GHRepository> shortcuts = null;
		if(catbot != null)
		{
			requests = catbot.requests;
			superUser = catbot.superUser;
			authorisedUsers = catbot.authorisedUsers;
			shortcuts = catbot.shortcuts;

		}
		catbot = new CatBot(requests, superUser, authorisedUsers, shortcuts);

		return "Catbot initialised!";
	}

}
