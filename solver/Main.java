package solver;

import java.io.*;
import java.util.regex.*;

public class Main {
    public static void main(String[] args) {
        String infile = args[1];
        debugWrite(infile, "debugout.txt");
        String outfile = args[3];
        Matrix matrix = new Matrix(infile);
        boolean infiniteFlag = false;
        for (int i = 0; i < matrix.m; i++) {
            int k = matrix.getNonZeroRow(i, i);
            if (k == -1) {
                infiniteFlag = true;
                break;
            }
            matrix.exchangeRow(k, i);
            matrix.normalizeRow(i, i);
            matrix.zeroBelowRow(i, i);
        }
        for (int i = matrix.m - 1; !infiniteFlag && i >= 0; i--) {
            matrix.zeroAboveRow(i, i); 
        }

        matrix.writeSolution(outfile,infiniteFlag);

    }

    static void debugWrite(String infile, String outfile) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(infile));
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outfile)));
            String rec;
            while ((rec = reader.readLine()) != null) {
                writer.println(rec);
            }
            reader.close();
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }
}

class Matrix {
    int n;
    int m;
    Complex[][] a;

    Matrix (String infile) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(infile));
            String rec = reader.readLine();
            String[] strs = rec.split(" ");
            m = Integer.parseInt(strs[0]);
            n = Integer.parseInt(strs[1]);
            a = new Complex[n][m + 1];
            for (int i = 0; i < n; i++) {
                rec = reader.readLine();
                strs = rec.split(" ");
                for (int j = 0; j < m + 1; j++) {
                    a[i][j] = new Complex(strs[j]);
                }
            } 
            reader.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }            
    }

    int getNonZeroRow(int row, int column) {
        for (int i = row; i < n; i++) {
            if (!a[i][column].isZero()) {
                return i;
            }
        }
        return -1;
    }

    void exchangeRow(int srow, int drow) {
        Complex welm;

        if (srow == drow) {
            return;
        }
        
        for (int j = 0; j < m + 1; j++) {
            welm = a[drow][j] ;
            a[drow][j] = a[srow][j];
            a[srow][j] = welm;
        }
    }
 
    void normalizeRow(int row, int column) {
        Complex pivot = a[row][column].clone();
        for (int j = column; j < m + 1; j++) {
            a[row][j].div(pivot);
        }
    }

    void zeroBelowRow(int row, int column) {
        for (int i = row + 1; i < n; i++) {
            Complex pivot = a[i][column].clone();
            for (int j = column; j < m + 1; j++) {
                Complex w = a[row][j].clone();
                w.mul(pivot);
                a[i][j].sub(w);
            }
        }
    }

    void zeroAboveRow(int row, int column) {
        for (int i = row - 1; i >= 0; i--) {
            Complex pivot = a[i][column].clone();
            Complex w = a[row][column].clone();
            w.mul(pivot);
            a[i][column].sub(w);
            w = a[row][m].clone();
            w.mul(pivot);
            a[i][m].sub(w);
        }
    }

    Complex[] getSolution() {
        Complex[] solution = new Complex[n];
        for (int i = 0; i < n; i++) {
            solution[i] = a[i][m];
        }
        return solution;
    } 

    void writeSolution(String outfile, boolean infiniteFlag) {
        Complex[] solution = getSolution();
        boolean nosolutionFlag = false;

        for (int i = 0; i < n; i++) {
            if (isZeroRow(i) && !a[i][m].isZero()) {
                nosolutionFlag = true;
                break;
            }
        }

        try {
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outfile)));
            if (nosolutionFlag) {
                writer.println("No solutions");
            } else if (infiniteFlag) {
                writer.println("Infinitely many solutions");
            } else {
                for (int i = 0; i < m; i++) {
                    writer.println(solution[i]);
                }
            }
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    boolean isZeroRow(int row) {
        for (int j = 0; j < m; j++) {
            if (!a[row][j].isZero()) {
                return false;
            }
        }
        return true;
    }
}

class Complex {
    double real;
    double imaginary;

    Complex(String number) {
        Pattern pattern = Pattern.compile("[+-]?[0-9]*[.]?[0-9]*i");
        Matcher matcher = pattern.matcher(number);
        String imstr = "";
        String restr = "";
        if (matcher.find()) {
            imstr = matcher.group();
            restr = matcher.replaceFirst("");
        } else {
            restr = number;
        }
        if ("".equals(restr)) {
            real = 0;
        } else {
            real = Double.parseDouble(restr);
        }
        if ("".equals(imstr)) {
            imaginary = 0;
        } else {
            imstr = imstr.replace("i", "");
            if ("".equals(imstr) || "+".equals(imstr)) {
                imaginary = 1;
            } else if ("-".equals(imstr)) {
                imaginary = -1;
            } else {
                imaginary = Double.parseDouble(imstr);
            }
        }
    }

    Complex(double real, double imaginary) {
        this.real = real;
        this.imaginary = imaginary;
    }

    void add(Complex b) {
        real += b.real;
        imaginary += b.imaginary;
    } 

    void sub(Complex b) {
        real -= b.real;
        imaginary -= b.imaginary;
    } 

    void mul(Complex b) {
        double wreal = real * b.real - imaginary * b.imaginary;
        double wimaginary = real * b.imaginary + b.real * imaginary;
        real = wreal;
        imaginary = wimaginary;
    }
    
    void div(Complex b) {
        Complex denominator = b.clone();
        Complex dcon = b.clone();
        dcon.con();
        mul(dcon);
        denominator.mul(dcon);
        scalar(1 / denominator.real);
    } 

    void con() {
        imaginary *= -1;
    }

    public Complex clone() {
        return new Complex(real, imaginary);
    }

    boolean equals(Complex b) {
        if (real == b.real && imaginary == b.imaginary) {
            return true;
        }
        return false;
    }

    void scalar(double s) {
        real *= s;
        imaginary *= s;
    }

    boolean isZero() {
        if (real == 0 && imaginary == 0) {
            return true;
        }
        return false;
    }

    public String toString() {
        String str = "";
        if (imaginary == 0) {
            if (real == 0) {
                str = "0";
            } else {
                str = Double.toString(real);
            }
        } else if (real == 0) {
            if (imaginary == 1) {
                str = "i";
            } else if (imaginary == -1) {
                str = "-i";
            } else {
                str = Double.toString(imaginary) + "i";
            }
        } else {
            if (imaginary < 0) {
                str = Double.toString(real) + Double.toString(imaginary) + "i";
            } else {
                str = Double.toString(real) + "+" + Double.toString(imaginary) + "i";
            }
        }
        
        return str;
    }
}

