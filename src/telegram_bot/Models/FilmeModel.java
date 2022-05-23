package telegram_bot.Models;

public class FilmeModel {
	private Object i;
	private String id;
	private String l;
	private String q;
	private int rank;
	private String s;
	private int y;
	
	public Object getI() {
		return i;
	}

	public String getId() {
		return id;
	}

	public String getNome() {
		return l;
	}

	public String getCategoria() {
		if(q == null) {
			return "-";
		}
		return q;
	}

	public int getRank() {
		return rank;
	}

	public String getAtoresPrincipais() {
		return s;
	}

	public int getAnoLancamento() {
		return y;
	}
	
}
