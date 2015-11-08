import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.swing.JFrame;


public class TuringMachine {
	static int steps=0;
	static enum Direction{
		LEFT,RIGHT,NULL
	}
	static boolean b=true;
	static int pos=0;
	static String state="0";
	static List<Character>tape=new ArrayList<>();
	static List<StateTransition>rules=new ArrayList<>();
	static class StateTransition{
		String fromState;
		char fromSymbol;
		String toState;
		char toSymbol;
		Direction d;
		boolean halt=false;
		int value(){
			int i=0;
			if(fromSymbol=='*'){
				i++;
			}
			if(fromState.equals("*")){
				i+=2;
			}
			return i;
		}
		static StateTransition valueOf(String s){
			String[]r=s.split(" ");
			StateTransition t=new StateTransition();
			if(r.length!=5){
				throw new IllegalArgumentException();
			}
			t.toState=r[4];
			t.fromState=r[0];
			if(r[1].length()!=1){
				throw new IllegalArgumentException();
			}
			if(r[2].length()!=1){
				throw new IllegalArgumentException();
			}
			t.fromSymbol=r[1].charAt(0);
			if(t.fromSymbol=='_')t.fromSymbol=' ';
			t.toSymbol=r[2].charAt(0);
			if(t.toSymbol=='_')t.toSymbol=' ';
			if(r[3].equals("r")){
				t.d=Direction.RIGHT;
			}else if(r[3].equals("l")){
				t.d=Direction.LEFT;
			}else if(r[3].equals("*")){
				t.d=Direction.NULL;
			}else{
				throw new IllegalArgumentException();
			}
			if(r[4].startsWith("halt"))t.halt=true;
			return t;
		}
		boolean matches(String state,char symbol){
			return (fromState.equals(state)||fromState.equals("*"))&&(fromSymbol==symbol||fromSymbol=='*');
		}
		void apply(){
			if(!toState.equals("*")){
				state=toState;
			}
			if(toSymbol!='*'){
				tape.set(pos,toSymbol);
			}
			if(d==Direction.RIGHT){
				pos++;
				while(pos>=tape.size())tape.add(' ');
			}
			if(d==Direction.LEFT){
				pos--;
				if(pos<0){
					pos=0;
					tape.add(0,' ');
				}
			}
			if(halt){
				Character[]t=tape.toArray(new Character[0]);
				int i=0;
				while(t[i]==' ')i++;
				int j=t.length-1;
				while(t[j]==' ')j--;
				for(int k=i;k<=j;k++)System.out.print(t[k]);
				System.out.println();
				System.out.println("Terminated in state "+toState);
				System.exit(0);
			}
		}
		static final Comparator<StateTransition>comparator=(a,b)->a.value()-b.value();
	}
	static class Screen extends Component{
		public Screen(){
			this.setBounds(0,0,500,500);
			this.setBackground(new Color(210,210,200));
		}
		private static final long serialVersionUID = 1L;
		public void paint(Graphics g){
			super.paint(g);
			while(pos>=tape.size())tape.add(' ');
			Character[]t=tape.toArray(new Character[0]);
			int i=0,x=5;
			while(t[i]==' '&&i<t.length-1)i++;
			int j=t.length-1;
			while(t[j]==' '&&j>=0)j--;
			int q;
			for(int k=q=Math.min(pos,Math.max(i,pos-9));k<=Math.max(pos,Math.min(j,pos+9));k++){
				g.drawRect(x-2,5,1,15);
				g.drawString(""+t[k],x,20);
				x+=10;
			}
			g.drawString("state: "+state+"   steps run: "+steps,4,50);
			g.drawString("^",5+10*(pos-q),35);
			b=true;
		}
	}
	public static void main(String[]args) throws IOException{
		Scanner reader=new Scanner(new File(args.length>0?args[0]:"code.txt"));
		Scanner in=new Scanner(System.in);
		while(reader.hasNext()){
			String line=reader.nextLine();
			if(!line.isEmpty())rules.add(StateTransition.valueOf(line));
		}
		reader.close();
		rules.sort(StateTransition.comparator);
		for(char c:in.nextLine().toCharArray())tape.add(c);
		in.close();
		Screen s=new Screen();
		JFrame f=new JFrame();
		f.setBounds(0,0,500,500);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setAlwaysOnTop(true);
		f.setVisible(true);
		f.add(s);
		tape.add(' ');
		l:while(true){
			while(pos>=tape.size())tape.add(' ');
			for(StateTransition t:rules){
				if(t.matches(state,tape.get(pos))){
					t.apply();
					steps++;
					b=false;
					f.repaint(10);
					while(!b){
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {}
					};
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {}
					continue l;
				}
			}
			throw new IllegalStateException("Could not find rule for state "+state+" symbol "+(tape.get(pos)==' '?'_':tape.get(pos)));
		}
	}
}