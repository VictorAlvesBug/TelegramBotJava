package telegram_bot.Models;

import java.util.List;

public class BuscaFilmeModel {
	private List<FilmeModel> d;
	private String q;
	private int v;
	
	public List<FilmeModel> getListaFilmes(){
		return this.d;
	}
	
	public String getTermoBuscado(){
		return this.q;
	}
	
	public int getV(){
		return this.v;
	}
}
