package telegram_bot.Models;

public class DetalhesFilmeModel {
	private String id;
	private ImagemFilmeModel image;
	private int runningTimeInMinutes;
	private int numberOfEpisodes;
	private int seriesStartYear;
	private int seriesEndYear;
	private String title;
	private int year;
	
	public String getId() {
		return id;
	}
	
	public ImagemFilmeModel getImage() {
		return image;
	}
	
	public int getRunningTimeInMinutes() {
		return runningTimeInMinutes;
	}
	
	public int getNumberOfEpisodes() {
		return numberOfEpisodes;
	}
	
	public int getSeriesStartYear() {
		return seriesStartYear;
	}
	
	public int getSeriesEndYear() {
		return seriesEndYear;
	}
	
	public String getTitle() {
		return title;
	}
	
	public int getYear() {
		return year;
	}
}
