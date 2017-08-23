package stuff;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;

public class CatBot
{
	Map<String, RepoTS> requests;
	String superUser;
	List<String> authorisedUsers;
	Map<String, GHRepository> shortcuts;
	String user;
	String password;
	String token;
	//ByteArrayOutputStream console;
	boolean fetching = false;
	public CatBot(Map<String, RepoTS> requestsc, String superUserc, List<String> authorisedUsersc, Map<String, GHRepository> shortcutsc)
	{
		//console = new ByteArrayOutputStream();
		//PrintStream ps = new PrintStream(console);
		//System.setOut(ps);

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

		if(requestsc != null)
		{
			requests = requestsc;
		}
		else
		{
			requests = new HashMap<>();
		}
		if(shortcutsc != null)
		{
			shortcuts = shortcutsc;
		}
		else
		{
			shortcuts = new HashMap<>();
		}
		if(authorisedUsersc != null)
		{
			authorisedUsers = authorisedUsersc;
		}
		else
		{
			authorisedUsers = new ArrayList<>();
		}
		if(superUserc != null)
		{
			superUser = superUserc;
		}
		else
		{
			superUser = "ihaveahax";
		}


		DiscordAPI api = Javacord.getApi(token, true);

		// connect
        api.connect(new FutureCallback<DiscordAPI>() {
            @Override
            public void onSuccess(DiscordAPI api) {
                // register listener
                api.registerListener(new MessageCreateListener() {
                    @Override
                    public void onMessageCreate(DiscordAPI api, final Message message) {
                    	GHRepository r = shortcuts.get(message.getContent().split(" ")[0].substring(1));

                    	//allows for it to animate .... as it is loading
                    	if(message.getContent().contains("Fetching"))
                    	{
                    		while(fetching)
                    		{
                    			try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
                    			message.edit(message.getContent() + ".");
                    		}
                    	}
                    	//if not a command, get out
                    	if(message.getContent().toCharArray()[0] != '$')
		                {
                    		return;
		                }
                    	//if a shortcut that we have previously entered
                    	else if(r != null)
                    	{
                    		String temp = "";
                    		try{temp = r.getOwner().getLogin();}catch(Exception e){e.printStackTrace();}
                    		final String author = temp;
                    		final String project = r.getName();
                    		Calendar c = Calendar.getInstance();
                    		c.add(Calendar.MINUTE, -10);
                    		Date d = c.getTime();
                    		RepoTS repo = requests.get(author + "/" + project);
                    		if(repo == null || repo.getTimestamp().before(d)) message.reply("Fetching data, please wait.");
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
                    	//add new authorised user
                    	else if(message.getAuthor().getName().equals(superUser) && message.getContent().split(" ")[0].equals("$adduser"))
                    	{
                    		String[] list = message.getContent().split(" ");
                        	if(list.length < 2)
                        	{
                        		message.reply("You need to supply an argument - the user name of the person you want to add to the authorised users list");
                        	}
                        	else
                        	{
                        		boolean added = authorisedUsers.add(list[1]);
                        		String thing = added ? "Successfully added " + list[1] : "Could not add " + list[1];
                        		message.reply(thing);
                        	}
                    	}
                    	//remove authorised user
                    	else if(message.getAuthor().getName().equals(superUser) && message.getContent().split(" ")[0].equals("$removeuser"))
                    	{
                    		String[] list = message.getContent().split(" ");
                        	if(list.length < 2)
                        	{
                        		message.reply("You need to supply an argument - the user name of the person you want to remove from the authorised users list");
                        	}
                        	else
                        	{
                        		boolean removed = authorisedUsers.remove(list[1]);
                        		String thing = removed ? "Successfully removed " + list[1] : "Could not remove " + list[1];
                        		message.reply(thing);
                        	}

                    	}

                    	//adding a repo to the shortcuts
                    	else if(message.getContent().split(" ")[0].equalsIgnoreCase("$s") && authorisedUsers.contains(message.getAuthor().getName()))
                    	{
                    		String[] list = message.getContent().split(" ");
                        	if(list.length < 4)
                        	{
                        		message.reply("You need to supply three arguments - 1st, shortcut, 2nd, author, 3rd, repository.");
                        	}
                        	else
                        	{
                        		String shortcut = list[1];
                        		String author = list[2];
                        		String repo = list[3];
                    			r = null;
                    			try
                    			{
                    				GitHub github = GitHub.connectUsingPassword(user, password);
                    				r = github.getRepository(author + "/" + repo);
                    				if(r == null)
                    				{
                    					message.reply("Repository " + repo + " by " + author + " not found");
                    				}
                    				else
                    				{
                    					shortcuts.put(shortcut, r);
                    					message.reply(repo + " by " + author + " added successfully with shortcut $" + shortcut);
                    				}
                    			}
                    			catch(Exception e)
                    			{
                    				e.printStackTrace();
                    			}
                        	}
                    	}
                    	//simple command to check to see if bot is awake
                    	else if(message.getContent().equalsIgnoreCase("$ping"))
                        {
                        	message.reply("pong");
                        }
                    	//display help
                        else if(message.getContent().equalsIgnoreCase("$help"))
                        {
                        	String thing = "$r author project = latest release of the specified project by the specified author\n"
                        			+ "$c author project = latest commit of the specified project by the specified author";
                        	if(authorisedUsers.contains(message.getAuthor().getName()))
                        	{
                        		thing += "\n$s shortcut author project = add shortcut to specified repository with the command 'shortcut' (don't include the '$')";
                        	}
                        	if(message.getAuthor().getName().equals(superUser))
                        	{
                        		thing += "\n$adduser user = add specified user to authorised users list (they can add repository shortcuts)"
                        				+ "\n$removeuser user = remove specified user from authorised users list";
                        	}
                        	message.reply("**Commands:**\n" + thing);

                        }
                    	//query latest release
                        else if(message.getContent().split(" ")[0].equalsIgnoreCase("$r"))
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
                        		RepoTS repo = requests.get(author + "/" + project);
                        		if(repo == null || repo.getTimestamp().before(d)) message.reply("Fetching data, please wait.");
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
                    	//query latest commit
                        else if(message.getContent().split(" ")[0].equalsIgnoreCase("$c"))
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
                        		RepoTS repo = requests.get(author + "/" + project);
                        		if(repo == null || repo.getTimestamp().before(d)) message.reply("Fetching data, please wait.");
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
                        	if(!message.getAuthor().isYourself()) message.reply("Unrecognised command. Use \"$help\" to see a list of commands.");
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

	/*public String getConsole()
	{
		return console.toString();
	}*/

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
			if(commit != null && release == null && isRelease) return project + " by " + author + " has no releases yet.";
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
