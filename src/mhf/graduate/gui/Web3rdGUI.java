/**
 * Generate GUI for this project
 */
package mhf.graduate.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import mhf.graduate.analyzer.DataSet;
import mhf.graduate.analyzer.Preprocess;
import mhf.graduate.analyzer.Report;
import mhf.graduate.crawler.Crawler;


/**
 * @author Haifei
 *
 */
public class Web3rdGUI {
	private JTextArea text;
	
	private void createGUI(){
		JFrame frame = new JFrame("Web����������̽�������--�Ѻ���");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setSize(400, 480);
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(3,2));
		
		JLabel label = new JLabel("Web����������̽�������");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setFont(new Font("SansSerif",Font.BOLD, 16));
		
		JLabel empty1 = new JLabel("   ");
		JLabel empty2 = new JLabel("   ");
		
		text = new JTextArea("��־��ʾ����");
		text.setEditable(false);
		text.setLineWrap(true);
		
		JScrollPane scroll = new JScrollPane(text);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		JButton buttonCrawl = new JButton("�ռ���������վ����");
		JButton buttonChina = new JButton("������վ����Ԥ����");
		JButton buttonGlobal = new JButton("������վ����Ԥ����");
		JButton buttonData = new JButton("����CookieSet���ݼ�");
		JButton buttonReportChina = new JButton("ʶ��׷���ߣ�������վ��");
		JButton buttonReportGlobal = new JButton("ʶ��׷���ߣ�������վ��");
		
		Font font = new Font("Dialog",Font.PLAIN, 12);
		buttonCrawl.setFont(font);
		buttonChina.setFont(font);
		buttonGlobal.setFont(font);
		buttonData.setFont(font);
		buttonReportChina.setFont(font);
		buttonReportGlobal.setFont(font);
		
		// add action listener for buttons
		buttonCrawl.addActionListener(new btListenerCrawl());
		buttonChina.addActionListener(new btListenerChina());
		buttonGlobal.addActionListener(new btListenerGlobal());
		buttonData.addActionListener(new btListenerData());
		buttonReportChina.addActionListener(new btListenerReportChina());
		buttonReportGlobal.addActionListener(new btListenerReportGlobal());
		
		panel.add(buttonCrawl);
		panel.add(buttonChina);
		panel.add(buttonGlobal);
		panel.add(buttonData);
		panel.add(buttonReportChina);
		panel.add(buttonReportGlobal);
		
		frame.add(label,BorderLayout.NORTH);
		frame.add(scroll,BorderLayout.CENTER);
		frame.add(panel,BorderLayout.SOUTH);
		frame.add(empty1, BorderLayout.WEST);
		frame.add(empty2, BorderLayout.EAST);
		frame.setVisible(true);
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Web3rdGUI wGui= new Web3rdGUI();
		wGui.createGUI();
	}
	
	private class btListenerCrawl implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			Crawler crawlerChina = new Crawler("China");
			crawlerChina.runCrawler(text);
			Crawler crawlerGlobal = new Crawler("Global");
			crawlerGlobal.runCrawler(text);
		}
		
	}
	
	private class btListenerChina implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			Preprocess classify = new Preprocess("China");
			classify.classify(text);
		}
		
	}
	
	private class btListenerGlobal implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			Preprocess classify = new Preprocess("Global");
			classify.classify(text);
		}
		
	}
	
	private class btListenerData implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			text.setText("��־��ʾ����\n���ڴ���������վ���ݼ������Ժ�...\n");
			text.paintImmediately(text.getBounds());

			DataSet dsChina = new DataSet("China");
			String pathChina = dsChina.generateDataSet();
			text.append("������վ���ݼ��Ѵ����ɹ����ļ�����·��Ϊ��\n"+pathChina);
			text.append("\n\n\n���ڴ���������վ���ݼ������Ժ�...\n");
			text.paintImmediately(text.getBounds());
			DataSet dsGlobal = new DataSet("Global");
			String pathGlobal = dsGlobal.generateDataSet();
			text.append("������վ���ݼ��Ѵ����ɹ����ļ�����·��Ϊ��\n"+pathGlobal);
		}
		
	}
	
	private class btListenerReportChina implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			Report r = new Report("China");
			r.generateReport(text);
		}
		
	}
	
	private class btListenerReportGlobal implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			Report r = new Report("Global");
			r.generateReport(text);
		}
		
	}
}
