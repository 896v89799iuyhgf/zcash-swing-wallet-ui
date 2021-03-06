/************************************************************************************************
 *  _________          _     ____          _           __        __    _ _      _   _   _ ___
 * |__  / ___|__ _ ___| |__ / ___|_      _(_)_ __   __ \ \      / /_ _| | | ___| |_| | | |_ _|
 *   / / |   / _` / __| '_ \\___ \ \ /\ / / | '_ \ / _` \ \ /\ / / _` | | |/ _ \ __| | | || |
 *  / /| |__| (_| \__ \ | | |___) \ V  V /| | | | | (_| |\ V  V / (_| | | |  __/ |_| |_| || |
 * /____\____\__,_|___/_| |_|____/ \_/\_/ |_|_| |_|\__, | \_/\_/ \__,_|_|_|\___|\__|\___/|___|
 *                                                 |___/
 *
 * Copyright (c) 2016 Ivan Vaklinov <ivan@vaklinov.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 **********************************************************************************/
package com.vaklinov.zcashui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EtchedBorder;


/**
 * Table to be used for transactions - specifically.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class TransactionTable 
	extends DataTable 
{	
	public TransactionTable(final Object[][] rowData, final Object[] columnNames, 
			                final JFrame parent, final ZCashClientCaller caller)
	{
		super(rowData, columnNames);
		
		JMenuItem showDetails = new JMenuItem("Show details...");
        popupMenu.add(showDetails);
        
        showDetails.addActionListener(new ActionListener() 
        {	
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				if ((lastRow >= 0) && (lastColumn >= 0))
				{
					try
					{
						String txID = TransactionTable.this.getModel().getValueAt(lastRow, 6).toString();
						txID = txID.replaceAll("\"", ""); // In case it has quotes
						
						System.out.println("Transaction ID for detail dialog is: " + txID);
						Map<String, String> details = caller.getRawTransactionDetails(txID);
						String rawTrans = caller.getRawTransaction(txID);
						
						DetailsDialog dd = new DetailsDialog(parent, details);
						dd.setVisible(true);
					} catch (Exception ex)
					{
						ex.printStackTrace();
						// TODO: report exception to user
					}
				} else
				{
					// Log perhaps
				}
			}
		});
        
        
		JMenuItem showInExplorer = new JMenuItem("Show in block explorer");
        popupMenu.add(showInExplorer);
        
        showInExplorer.addActionListener(new ActionListener() 
        {	
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				if ((lastRow >= 0) && (lastColumn >= 0))
				{
					try
					{
						String txID = TransactionTable.this.getModel().getValueAt(lastRow, 6).toString();
						txID = txID.replaceAll("\"", ""); // In case it has quotes
						
						System.out.println("Transaction ID for block explorer is: " + txID);
						// https://explorer.zcha.in/transactions/<ID>
						Desktop.getDesktop().browse(
							new URL("https://explorer.zcha.in/transactions/" + txID).toURI());
					} catch (Exception ex)
					{
						ex.printStackTrace();
						// TODO: report exception to user
					}
				} else
				{
					// Log perhaps
				}
			}
		});
	} // End constructor

	
	
	private static class DetailsDialog
		extends JDialog
	{
		public DetailsDialog(JFrame parent, Map<String, String> details)
			throws UnsupportedEncodingException
		{
			this.setTitle("Transaction details...");
			this.setSize(600,  310);
		    this.setLocation(100, 100);
			this.setLocationRelativeTo(parent);
			this.setModal(true);
			this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			
			this.getContentPane().setLayout(new BorderLayout(0, 0));
			
			JPanel tempPanel = new JPanel(new BorderLayout(0, 0));
			tempPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
			JLabel infoLabel = new JLabel(
					"<html><span style=\"font-size:9px;\">" +
					"The table shows the information about the transaction with technical details as " +
					"they appear at ZCash network level." +
				    "</span>");
			infoLabel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			tempPanel.add(infoLabel, BorderLayout.CENTER);
			this.getContentPane().add(tempPanel, BorderLayout.NORTH);
			
			String[] columns = new String[] { "Name", "Value" };
			String[][] data = new String[details.size()][2];
			int i = 0;
			int maxPreferredWidht = 400;
			for (Entry<String, String> ent : details.entrySet())
			{
				if (maxPreferredWidht < (ent.getValue().length() * 6))
				{
					maxPreferredWidht = ent.getValue().length() * 6;
				}
				
				data[i][0] = ent.getKey();
				data[i][1] = ent.getValue();
				i++;
			}
			
			Arrays.sort(data, new Comparator<String[]>() 
			{
			    public int compare(String[] o1, String[] o2)
			    {
			    	return o1[0].compareTo(o2[0]);
			    }

			    public boolean equals(Object obj)
			    {
			    	return false;
			    }
			});
			
			DataTable table = new DataTable(data, columns);
			table.getColumnModel().getColumn(0).setPreferredWidth(200);
			table.getColumnModel().getColumn(1).setPreferredWidth(maxPreferredWidht);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			JScrollPane tablePane = new JScrollPane(
				table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			
			this.getContentPane().add(tablePane, BorderLayout.CENTER);

			// Lower close button
			JPanel closePanel = new JPanel();
			closePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 3));
			JButton closeButon = new JButton("Close");
			closePanel.add(closeButon);
			this.getContentPane().add(closePanel, BorderLayout.SOUTH);

			closeButon.addActionListener(new ActionListener()
			{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						DetailsDialog.this.setVisible(false);
						DetailsDialog.this.dispose();
					}
			});

		}
		
		
	}
}
