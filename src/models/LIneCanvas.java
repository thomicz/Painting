package models;

import java.util.ArrayList;
import java.util.List;

public class LIneCanvas {
    private List<Line> lines;
    public LIneCanvas(){
        lines = new ArrayList<Line>();
    }
    public List<Line> getLines(){
        return lines;
    }
    public void setLines(List<Line> lines){
        lines.clear();
    }
    public void addLine(Line line){
        lines.add(line);
    }
}
