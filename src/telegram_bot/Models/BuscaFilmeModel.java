package telegram_bot.Models;

import java.util.List;

public class BuscaFilmeModel {
	private List<FilmeModel> results;
	private String query;
	
	public List<FilmeModel> getListaFilmes(){
		return results.stream().filter(filme -> filme.getNome() != null).toList();
	}
	
	public String getTermoBuscado(){
		return query;
	}
}
