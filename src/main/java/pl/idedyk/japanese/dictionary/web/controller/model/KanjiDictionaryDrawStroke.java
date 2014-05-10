package pl.idedyk.japanese.dictionary.web.controller.model;

import java.util.ArrayList;
import java.util.List;

public class KanjiDictionaryDrawStroke {
	
	private List<List<Point>> strokes = new ArrayList<List<Point>>();
	
	public void newStroke() {
		strokes.add(new ArrayList<Point>());
	}
	
	public void addPoint(Integer x, Integer y) {
		strokes.get(strokes.size() - 1).add(new Point(x, y));
	}
	
	public List<List<Point>> getStrokes() {
		return strokes;
	}

	public void setStrokes(List<List<Point>> strokes) {
		this.strokes = strokes;
	}

	public static class Point {
		
		private Integer x;
		
		private Integer y;
		
		public Point() { }

		public Point(Integer x, Integer y) {
			super();
			this.x = x;
			this.y = y;
		}

		public Integer getX() {
			return x;
		}

		public void setX(Integer x) {
			this.x = x;
		}

		public Integer getY() {
			return y;
		}

		public void setY(Integer y) {
			this.y = y;
		}		
	}
}
