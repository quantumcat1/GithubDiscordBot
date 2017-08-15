package stuff;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.SwingWorker;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;

import com.google.common.util.concurrent.FutureCallback;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;

public class CatBot
{
	Map<String, RepoTS> requests;
	String user;
	String password;
	String token;
	boolean fetching = false;
	public CatBot()
	{
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try (InputStream input = classLoader.getResourceAsStream("config.properties"))
		{
			Properties prop = new Properties();
			prop.load(input);

			user = prop.getProperty("user");
			password = prop.getProperty("password");
			token = prop.getProperty("token");
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		requests = new HashMap<String, RepoTS>();

		DiscordAPI api = Javacord.getApi(token, true);

		// connect
        api.connect(new FutureCallback<DiscordAPI>() {
            @Override
            public void onSuccess(DiscordAPI api) {
                // register listener
                api.registerListener(new MessageCreateListener() {
                    @Override
                    public void onMessageCreate(DiscordAPI api, final Message message) {
                    	//if(message.getChannelReceiver() != null) return;
                    	if(message.getContent().contains("Fetching"))
                    	{
                    		while(fetching)
                    		{
                    			try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
                    			message.edit(message.getContent() + ".");
                    		}
                    	}
                    	if (message.getContent().equalsIgnoreCase(".ping"))
                        {
                        	message.reply("pong");
                        }
                        else if (message.getContent().equalsIgnoreCase(".help"))
                        {
                        	message.reply("**Commands:**\n"
                        			+ ".r author project = latest release of the specified project by the specified author\n"
                        			+ ".c author project = latest commit of the specified project by the specified author\n"
                        			+ "\n**Shortcut repositories**"
                        			+ ".fbi = latest release of FBI by Steveice10\n"
                        			+ ".lumaupdater = latest release of Luma Updater by KunoichiZ\n"
                        			+ ".luma = latest release of Luma3DS by AuroraWright\n"
                        			+ ".reinand = latest release of ReiNAND by Reisyukaku\n"
                        			+ ".ntrboot = latest release of NTRboot flasher by kitling\n"
                        			+ ".gm9 = latest release of GodMode9 flasher by d0k3\n"
                        			+ ".b9s = latest release of Boot9Strap by SciresM\n"
                        			+ ".guide = latest commit of the Guide by Plailect"
                        			);
                        }
                        else if (message.getContent().equalsIgnoreCase(".fbi"))
                        {
                        	message.reply(message("Steveice10", "FBI", true));
                        }
                        else if (message.getContent().equalsIgnoreCase(".lumaupdater"))
                        {
                        	message.reply(message("KunoichiZ", "lumaupdate", true));
                        }
                        else if (message.getContent().equalsIgnoreCase(".reinand"))
                        {
                        	message.reply(message("Reisyukaku", "ReiNand", true));
                        }
                        else if (message.getContent().equalsIgnoreCase(".ntrboot"))
                        {
                        	message.reply(message("kitling", "ntrboot_flasher", true));
                        }
                        else if (message.getContent().equalsIgnoreCase(".luma"))
                        {
                        	message.reply(message("AuroraWright", "Luma3DS", true));
                        }
                        else if(message.getContent().equalsIgnoreCase(".gm9"))
                        {
                        	message.reply(message("d0k3", "GodMode9", true));
                        }
                        else if(message.getContent().equalsIgnoreCase(".guide"))
                        {
                        	message.reply(message("Plailect", "Guide", false));
                        }
                        else if(message.getContent().equalsIgnoreCase(".b9s"))
                        {
                        	message.reply(message("SciresM", "boot9strap", true));
                        }
                        else if(message.getContent().split(" ")[0].equalsIgnoreCase(".r"))
                        {
                        	String[] list = message.getContent().split(" ");
                        	if(list.length < 3)
                        	{
                        		message.reply("You need to supply two arguments - 1st, author, 2nd, repository name");
                        	}
                        	else
                        	{
                        		final String author = list[1];
                        		final String project = list[2];
                        		Calendar c = Calendar.getInstance();
                        		c.add(Calendar.MINUTE, -10);
                        		Date d = c.getTime();
                        		RepoTS r = requests.get(author + "/" + project);
                        		if(r == null || r.getTimestamp().before(d)) message.reply("Fetching data, please wait.");
                        		new SwingWorker<Void, Void> ()
                        		{
                        			String m;
									@Override
									protected Void doInBackground() throws Exception
									{
										fetching = true;
										m = message(author, project, true);
										return null;
									}
									@Override
									protected void done()
									{
										message.reply(m);
										fetching = false;
									}
                        		}.execute();
                        	}
                        }
                        else if(message.getContent().split(" ")[0].equalsIgnoreCase(".c"))
                        {
                        	String[] list = message.getContent().split(" ");
                        	if(list.length < 3)
                        	{
                        		message.reply("You need to supply two arguments - 1st, author, 2nd, repository name");
                        	}
                        	else
                        	{
                        		final String author = list[1];
                        		final String project = list[2];
                        		Calendar c = Calendar.getInstance();
                        		c.add(Calendar.MINUTE, -10);
                        		Date d = c.getTime();
                        		RepoTS r = requests.get(author + "/" + project);
                        		if(r == null || r.getTimestamp().before(d)) message.reply("Fetching, please wait!");
                        		new SwingWorker<Void, Void> ()
                        		{
                        			String m;
									@Override
									protected Void doInBackground() throws Exception
									{
										fetching = true;
										m = message(author, project, false);
										return null;
									}
									@Override
									protected void done()
									{
										message.reply(m);
										fetching = false;
									}
                        		}.execute();
                        	}
                        }
                        else
                        {
                        	if(!message.getAuthor().isYourself()) message.reply("Unrecognised command. Use \".help\" to see a list of commands.");
                        }
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        });
	}

	private GHCommit getLatestCommit(String lookup)
	{
		GHCommit latest = null;
		try
		{
			GitHub github = GitHub.connectUsingPassword(user, password);
			GHRepository repo = github.getRepository(lookup);
			PagedIterable<GHCommit> commits = repo.listCommits();
			List<GHCommit> list = commits.asList();
			if(list.size() > 0)
			{
				latest = list.get(0);
				for(GHCommit c : list)
				{
					if(c.getCommitDate().after(latest.getCommitDate()))
					{
						latest = c;
					}
				}
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return latest;
	}

	private GHRelease getLatestRelease(String lookup)
	{
		GHRelease latest = null;
		try
		{
			GitHub github = GitHub.connectUsingPassword(user, password);
			GHRepository repo = null;
			try
			{
				repo = github.getRepository(lookup);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return null;
			}
			List<GHRelease> releases = repo.getReleases();
			if(releases.size() > 0)
			{
				latest = releases.get(0);
				for(GHRelease r : releases)
				{
					if(r.getPublished_at().after(latest.getPublished_at()))
					{
						latest = r;
					}
				}
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return latest;
	}

	private String message(String author, String project, boolean isRelease)
	{
		String lookup = author + "/" + project;
		RepoTS r = requests.get(lookup);

		Calendar c = Calendar.getInstance();
		c.add(Calendar.MINUTE, -10);
		Date d = c.getTime();

		if(r == null || r.getTimestamp().before(d))
		{
			GHRelease release = getLatestRelease(lookup);
			GHCommit commit = getLatestCommit(lookup);
			if(commit == null) return "Author (" + author + ") and project (" + project + ") combination not found.";
			r = new RepoTS(release, commit, new Date());

			requests.put(lookup, r);
		}

		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
		String date = "";
		String link = "";
		String desc = "";
		if(isRelease)
		{
			date = sdf.format(r.getRelease().getPublished_at());
			link = "https://github.com/" + author + "/"
			+ project + "/releases/" + r.getRelease().getTagName();
			desc = r.getRelease().getBody();
		}
		else
		{
			try{date = sdf.format(r.getCommit().getCommitDate());}catch(Exception e){}
			link = "https://github.com/" + author + "/"
			+ project + "/commit/" + r.getCommit().getSHA1();
			try{desc = r.getCommit().getCommitShortInfo().getMessage();}catch (Exception e){}
		}
		String thing = isRelease ? "release" : "commit";
		String reply = project + " latest " + thing + ": " + date + "\n" +
				"<" + link + ">\n" + desc;
		return reply;
	}
}
