package telegram_bot.Models;

import java.util.List;

public class FilmeModel {
	private String id;
	private ImagemFilmeModel image;
	private String title;
	private String titleType;
	private int year;
	private List<AtorFilmeModel> principals;

	public String getId() {
		return id;
	}

	public String getUrlImagem() {
		if (image == null || image.getUrl() == null) {
			return "-";
		}
		return image.getUrl();
	}

	public String getNome() {
		return title;
	}

	public String getCategoria() {
		if (titleType == null) {
			return "-";
		}
		return titleType;
	}

	public int getAnoLancamento() {
		return year;
	}

	public String getAtoresPrincipais() {
		return String.join(", ", principals.stream().map(ator -> ator.getName()).toList());
	}

}
