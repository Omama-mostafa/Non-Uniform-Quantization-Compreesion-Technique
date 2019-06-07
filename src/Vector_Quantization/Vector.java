package Vector_Quantization;

import com.sun.xml.internal.messaging.saaj.soap.impl.HeaderImpl;

import javax.imageio.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Vector
{
	private JFrame frame1;
	private static int Imgheight;
	private static int Imgwidth;
	private static int vec_height;
	private static int vec_width;
	private static int Level_num;
	
	private static int[][] Balance_Img;
	private static ArrayList<int[][]> Average = new ArrayList<>();
	
	private static ArrayList<int[][]> temp = new ArrayList<>();
	private static ArrayList<Table> quantize = new ArrayList<>();
	private static ArrayList<String> comp_data = new ArrayList<>();
	private static ArrayList<ArrayList<int[][]>> Comp = new ArrayList<ArrayList<int[][]>>();
	
	private static ArrayList<int[][]> DeComp = new ArrayList<>();
	private static int[][] DeImage = new int[895][582];
	
	static class Node
	{
		ArrayList<int[][]> sub_arr = new ArrayList<>();
		int ave[][] = new int[vec_height][vec_width];
		Node left = null;
		Node right = null;
	}
	
	static class Table
	{
		int vec[][] = new int[vec_height][vec_width];
		String code = "";
	}
	
	public static void main(String[] args) throws IOException
	{
		/*EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					Vector window = new Vector();
					window.frame1.setVisible(true);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		*/
		Scanner read = new Scanner(System.in);
		System.out.print("Enter Number of Level : ");
		Level_num = read.nextInt();
		System.out.println("Enter Vector Size : ");
		System.out.print("Height : ");
		vec_height = read.nextInt();
		System.out.print("Width : ");
		vec_width = read.nextInt();
		
		int[][] image1 = read_image("Pic1.jpg");
		Balance_Image(image1, vec_height, vec_width);
		ConvertToArr();
		
		Node n = new Node();
		n.sub_arr = temp;
		n.ave = Arr_Average(temp);
		Split_Img(n, Level_num);
		Create_Average(n);
		Create_Table();
		Compression();
		Write_in_File();
		DeCompression();
		String file = "Output_Image1.jpg";
		ConvertToImage(DeImage, file);
	}
	
	/*public Vector()
	{
		initialize_Form();
	}
	
	private void initialize_Form()
	{
		frame1 = new JFrame();
		frame1.setBounds(100, 100, 600, 600);
		frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame1.getContentPane().setLayout(null);
		
		JLabel lblWel = new JLabel("Welcome");
		lblWel.setBounds(2, 1, 100, 50);
		frame1.getContentPane().add(lblWel);
		
		JLabel lbllvl = new JLabel("Enter Number of Level : ");
		lbllvl.setBounds(10, 50, 150, 20);
		frame1.getContentPane().add(lbllvl);
		
		
		JTextField textField_1 = new JTextField();
		textField_1.setBounds(200, 50, 150, 25);
		frame1.getContentPane().add(textField_1);
		textField_1.setColumns(20);
		
		JLabel lblH = new JLabel("Height : ");
		lblH.setBounds(10, 100, 100, 20);
		frame1.getContentPane().add(lblH);
		
		JTextField textField_2 = new JTextField();
		textField_2.setBounds(200, 100, 150, 25);
		frame1.getContentPane().add(textField_2);
		textField_2.setColumns(20);
		
		JLabel lblW = new JLabel("Width : ");
		lblW.setBounds(10, 150, 100, 20);
		frame1.getContentPane().add(lblW);
		
		JTextField textField_3 = new JTextField();
		textField_3.setBounds(200, 155, 150, 25);
		frame1.getContentPane().add(textField_3);
		textField_3.setColumns(20);
		
		JButton btnSubmit1 = new JButton("Continue...");
		btnSubmit1.setBounds(440, 420, 100, 30);
		frame1.getContentPane().add(btnSubmit1);
		
		btnSubmit1.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Level_num = Integer.parseInt(textField_1.getText());
				vec_height = Integer.parseInt(textField_2.getText());
				vec_width = Integer.parseInt(textField_3.getText());
				
				int[][] image1 = new int[0][];
				try
				{
					image1 = read_image("Pic1.jpg");
				}
				catch(IOException e1)
				{
					e1.printStackTrace();
				}
				Balance_Image(image1, vec_height, vec_width);
				ConvertToArr();
				
				Node n = new Node();
				n.sub_arr = temp;
				n.ave = Arr_Average(temp);
				Split_Img(n, Level_num);
				Create_Average(n);
				Create_Table();
				Compression();
				try
				{
					Write_in_File();
				}
				catch(IOException e1)
				{
					e1.printStackTrace();
				}
				try
				{
					DeCompression();
				}
				catch(IOException e1)
				{
					e1.printStackTrace();
				}
				String file = "Output_Image1.jpg";
				try
				{
					ConvertToImage(DeImage, file);
				}
				catch(IOException e1)
				{
					e1.printStackTrace();
				}
			}
		});
	}*/
	
	
	private static int[][] read_image(String filepath) throws IOException
	{
		File f = new File(filepath);
		int[][] img_arr = null;
		BufferedImage img = ImageIO.read(f);
		Imgwidth = img.getWidth();
		Imgheight = img.getHeight();
		img_arr = new int[Imgheight][Imgwidth];
		for(int h = 0; h < Imgheight; h++)
		{
			for(int w = 0; w < Imgwidth; w++)
			{
				int p = img.getRGB(w, h);
				int a = (p >> 24) & 0xff;
				int r = (p >> 16) & 0xff;
				int g = (p >> 8) & 0xff;
				int b = p & 0xff;
				
				img_arr[h][w] = r;
				p = (a << 24) | (r << 16) | (g << 8) | b;
				img.setRGB(w, h, p);
			}
		}
		return img_arr;
	}

	private static void Balance_Image(int main_vec[][], int VecHeight, int VecWidth)
	{
		int Height_Reminder = Imgheight % VecHeight;
		int Width_Reminder = Imgwidth % VecWidth;
		int Hdiff = 0, Wdiff = 0;
		if(Height_Reminder != 0)
			Hdiff = VecHeight - Height_Reminder;
		if(Width_Reminder != 0)
			Wdiff = VecHeight - Width_Reminder;
		
		Balance_Img = new int[Hdiff + Imgheight][Wdiff + Imgwidth];
		for(int i = 0; i < Imgheight; i++)
		{
			for(int j = 0; j < Imgwidth; j++)
			{
				Balance_Img[i][j] = (main_vec[i][j]);
			}
		}
		if(Height_Reminder != 0)
		{
			for(int i = 0; i < (Hdiff * Imgwidth); i++)
				Balance_Img[Imgheight][i] = 0;
		}
		if(Width_Reminder != 0)
		{
			for(int i = 0; i < (Wdiff * Imgheight); i++)
				Balance_Img[i][Imgwidth] = 0;
		}
		Imgheight += Hdiff;
		Imgwidth += Wdiff;
		/*for(int i = 0; i < Imgheight; i++)
		{
			for(int j = 0; j < Imgwidth; j++)
				System.out.print(Balance_Img[i][j] + "\t");
			System.out.println("\n");
		}*/
	}
	
	private static void ConvertToArr()
	{
		ArrayList<Integer> inner = new ArrayList<>();
		ArrayList<ArrayList<Integer>> outer = new ArrayList<>();
		int h = 0, w = 0;
		while(h != vec_height && w != vec_width)
		{
			for(int i = h; i < Imgheight; i += vec_height)
			{
				for(int j = w; j < Imgwidth; j += vec_width)
				{
					inner.add(Balance_Img[i][j]);
					if(i >= Imgheight - vec_height && j >= Imgwidth - vec_width)
					{
						outer.add(inner);
						inner = new ArrayList<>();
						w++;
						if(w == vec_width)
						{
							w = 0;
							h++;
						}
					}
				}
			}
		}
		int Size = (Imgheight * Imgwidth) / (vec_height * vec_height);
		for(int i = 0; i < Size; i++)
		{
			int vec_inner[][] = new int[vec_height][vec_width];
			int f = 0, l = 0;
			for(int j = 0; j < (vec_height * vec_width); j++)
			{
				vec_inner[f][l] = outer.get(j).get(i);
				if(l == vec_width - 1 && f == vec_height - 1)
				{
					temp.add(vec_inner);
				}
				if(l == vec_width - 1)
				{
					l = 0;
					f++;
				}
				else
					l++;
			}
		}
	}
	
	private static int[][] Arr_Average(ArrayList<int[][]> arr)
	{
		int ave[][] = new int[vec_height][vec_width];
		for(int i = 0; i < vec_height; i++)
		{
			for(int j = 0; j < vec_width; j++)
			{
				float val = 0;
				int n = 0;
				while(n != arr.size())
				{
					val += arr.get(n)[i][j];
					n++;
				}
				ave[i][j] = (int) val / arr.size();
			}
		}
		return ave;
	}
	
	
	private static void Split_Img(Node root, int level_num)
	{
		if(level_num == 0)
		{
		
		}
		else
		{
			int left_ave[][] = new int[vec_height][vec_width];
			int right_ave[][] = new int[vec_height][vec_width];
			for(int i = 0; i < vec_height; i++)
			{
				for(int j = 0; j < vec_width; j++)
				{
					left_ave[i][j] = (int) root.ave[i][j] - 1;
					right_ave[i][j] = (int) root.ave[i][j] + 1;
				}
			}
			root.left = new Node();
			root.right = new Node();
			for(int i = 0; i < root.sub_arr.size(); i++)
			{
				int h = 0, w = 0;
				int val_left = 0;
				int val_right = 0;
				while(h != vec_height - 1)
				{
					val_left += Math.abs(root.sub_arr.get(i)[h][w] - left_ave[h][w]);
					val_right += Math.abs(root.sub_arr.get(i)[h][w] - right_ave[h][w]);
					if(w == vec_width - 1)
					{
						w = 0;
						h++;
					}
					w++;
				}
				if(val_left < val_right)
				{
					root.left.sub_arr.add(root.sub_arr.get(i));
				}
				else
				{
					root.right.sub_arr.add(root.sub_arr.get(i));
				}
			}
			if(root.left.sub_arr.size() == 0)
			{
				root.left.ave = root.ave;
			}
			else
			{
				root.left.ave = Arr_Average(root.left.sub_arr);
			}
			if(root.right.sub_arr.size() == 0)
			{
				root.right.ave = root.ave;
			}
			else
			{
				root.right.ave = Arr_Average(root.right.sub_arr);
			}
			level_num--;
			Split_Img(root.left, level_num);
			Split_Img(root.right, level_num);
		}
	}
	
	private static void Create_Average(Node root)
	{
		if((root.left.left == null && root.left.right == null) || (root.right.right == null && root.right.left == null))
		{
			Comp.add(root.left.sub_arr);
			Comp.add(root.right.sub_arr);
			Average.add(root.left.ave);
			Average.add(root.right.ave);
			return;
		}
		Create_Average(root.left);
		Create_Average(root.right);
	}
	
	private static void Create_Table()
	{
		int n = 0;
		while(n != Average.size())
		{
			Table obj = new Table();
			obj.vec = Average.get(n);
			obj.code = Integer.toBinaryString(n);
			if(obj.code.length() != Level_num)
			{
				String c = "";
				for(int i = obj.code.length(); i < Level_num; i++)
					c += "0";
				obj.code = c + obj.code;
			}
			quantize.add(obj);
			n++;
		}
	}
	
	private static void Compression()
	{
		for(int n = 0; n < temp.size(); n++)
		{
			for(int i = 0; i < Comp.size(); i++)
			{
				if(Comp.get(i).contains(temp.get(n)))
				{
					comp_data.add(quantize.get(i).code);
					break;
				}
			}
		}
	}
	
	
	private static void ConvertToVec(ArrayList<int[][]> vec)
	{
		ArrayList<Integer> temp = new ArrayList<>();
		while(vec.size() != 1)
		{
			int h = 0, w = 0;
			for(int i = 0; i < (Imgwidth / vec_width); )
			{
				temp.add(vec.get(i)[h][w]);
				w++;
				if(w == vec_width)
				{
					w = 0;
					i++;
				}
				if(i == (Imgwidth / vec_width))
				{
					i = 0;
					h++;
					if(h == vec_height)
						break;
				}
			}
			int[] a = new int[Imgwidth / vec_width];
			for(int i = 0; i < (Imgwidth / vec_width); i++)
			{
				a[i] = i;
			}
			for(int i = a.length - 1; i >= 0 && vec.size() != 1; i--)
			{
				vec.remove(a[i]);
			}
		}
		int n = 0;
		for(int i = 0; i < 895; i++)
		{
			for(int j = 0; j <582; j++)
			{
				DeImage[i][j] = temp.get(n);
				n++;
			}
		}
	}
	
	
	private static void DeCompression() throws IOException
	{
		ArrayList<Table> dequantizer = new ArrayList<>();
		ArrayList<String> decode = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader("Overhead.txt"));
		BufferedReader Cbr = new BufferedReader(new FileReader("CompressionCode.txt"));
		String[] s;
		String read = br.readLine();
		while((read = br.readLine()) != null)
		{
			s = read.split(",");
			int n = 0;
			Table obj = new Table();
			for(int i = 0; i < vec_height; i++)
			{
				for(int j = 0; j < vec_width; j++)
				{
					obj.vec[i][j] = Integer.parseInt(s[n]);
					n++;
				}
			}
			obj.code = s[n];
			dequantizer.add(obj);
		}
		String code_read = "";
		String[] code_s;
		while((code_read = Cbr.readLine()) != null)
		{
			code_s = code_read.split(",");
			decode.addAll(Arrays.asList(code_s));
		}
		br.close();
		Cbr.close();
		
		for(int i = 0; i < decode.size(); i++)
		{
			for(int j = 0; j < dequantizer.size(); j++)
			{
				if(dequantizer.get(j).code.equals(decode.get(i)))
				{
					DeComp.add(dequantizer.get(j).vec);
					break;
				}
			}
		}
		ConvertToVec(DeComp);
	}
	
	private static void Write_in_File() throws IOException
	{
		File file2 = new File("Overhead.txt");
		File file3 = new File("CompressionCode.txt");
		BufferedWriter bw = new BufferedWriter(new FileWriter("Overhead.txt"));
		BufferedWriter Cbw = new BufferedWriter(new FileWriter("CompressionCode.txt"));
		
		bw.write("Average Vector" + "\t" + "Code");
		for(int i = 0; i < quantize.size(); i++)
		{
			bw.newLine();
			int h = 0, w = 0;
			while(h != vec_height)
			{
				bw.write(quantize.get(i).vec[h][w] + ",");
				w++;
				if(w == vec_width)
				{
					w = 0;
					h++;
				}
			}
			bw.write(quantize.get(i).code);
		}
		for(int i = 0; i < comp_data.size(); i++)
		{
			Cbw.write(comp_data.get(i) + ",");
		}
		bw.close();
		Cbw.close();
		
	}
	
	private static void ConvertToImage(int[][] img, String imgOutput) throws IOException
	{
		int height = img.length;
		int width = img[0].length;
		BufferedImage im = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		for(int i = 0; i < height; i++)
		{
			for(int j = 0; j < width; j++)
			{
				int a = 255;
				int pix = img[i][j];
				int p = (a << 24) | (pix << 16) | (pix << 8) | pix;
				im.setRGB(j, i, p);
			}
		}
		File f = new File(imgOutput);
		if(!f.exists())
			f.createNewFile();
		ImageIO.write(im, "jpg", f);
	}
}

