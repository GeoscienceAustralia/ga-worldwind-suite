package application;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.retrieve.RetrievalService;
import gov.nasa.worldwind.retrieve.Retriever;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JTextArea;

public class DownloadStatus extends JFrame
{
	private JTextArea text;

	public DownloadStatus()
	{
		super("Download status");
		setLayout(new BorderLayout());

		text = new JTextArea();
		add(text, BorderLayout.CENTER);

		setSize(800, 600);

		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				while (true)
				{
					update();
					try
					{
						Thread.sleep(100);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
		
		RetrievalService rs = WorldWind.getRetrievalService();
		rs.addPropertyChangeListener(new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent evt)
			{
				update();
			}
		});
	}

	public void update()
	{
		RetrievalService rs = WorldWind.getRetrievalService();
		if (rs instanceof ReportingRetrievalService)
		{
			ReportingRetrievalService rrs = (ReportingRetrievalService) rs;
			Set<Retriever> retrievers = rrs.getRetrievers();
			StringBuilder sb = new StringBuilder();
			for (Retriever retriever : retrievers)
			{
				int cl = retriever.getContentLength();
				if (cl > 0)
				{
					sb
							.append((100 * retriever.getContentLengthRead() / retriever
									.getContentLength())
									+ "% - ");
				}
				sb.append(retriever.getName() + "\n");
			}
			text.setText(sb.toString());
		}
	}
}
