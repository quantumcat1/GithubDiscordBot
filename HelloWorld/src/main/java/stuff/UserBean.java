package stuff;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;

@ManagedBean
@RequestScoped
public class UserBean {
	private String password;
	private boolean success = false;
	private String token;
	private boolean submitted = false;

	@ManagedProperty(value = "#{master}")
	private Master master;

	public boolean checkPassword() {
		return master.checkPassword(password);
	}

	public void changeToken() {
		if (checkPassword()) {
			success = true;
			master.changeToken(token);
		} else {
			success = false;
		}
		submitted = true;
	}

	public String getText() {
		if (success)
			return "Token successfully changed - now start/restart CatBot";
		return "Password incorrect, token not changed";
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public boolean isSubmitted() {
		return submitted;
	}

	public void setSubmitted(boolean submitted) {
		this.submitted = submitted;
	}

	public Master getMaster() {
		return master;
	}

	public void setMaster(Master master) {
		this.master = master;
	}
}
