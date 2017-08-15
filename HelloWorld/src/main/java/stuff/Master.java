package stuff;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

@ManagedBean
@ApplicationScoped
public class Master implements Serializable
{
	private static final long serialVersionUID = -9082917625309737085L;
	private CatBot catbot;

	@PostConstruct
	public void initialise()
	{
		catbot = new CatBot();
	}

	public String getHello()
	{
		return "CatBot initialised!";
	}

}
