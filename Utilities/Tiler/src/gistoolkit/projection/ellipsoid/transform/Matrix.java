/*
 *    GISToolkit - Geographical Information System Toolkit
 *    (C) 2002, Ithaqua Enterprises Inc.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; 
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *    
 */

package gistoolkit.projection.ellipsoid.transform;

/** Class to contain matrix manipulation logic */
/* Node, I did not write this, it is stollen, ripped off, or pirated from another unknown source if you know who let me know so I can give proper credit.*/
public class Matrix  
{
    private int rows, cols;
    private double M[][];
    
    /** Return the number of rows in the matrix */
    public int getRowNum(){return rows;}
    /** Return the number of columns in the matrix */
    public int getColNum(){return cols;}
    /** Return the value at the given indexes */
    public double getValue(int inRow, int inColumn){return M[inRow][inColumn];}
    
    /** Create a new matrix with the given number of rows and columns */
    public Matrix(int tRows, int tCols, double T[][]){
        M = new double[tRows][tCols];
        rows = tRows;
        cols = tCols;
        
        for(int i=0; i<rows; i++)
            for(int j=0; j<cols; j++)
                M[i][j] = T[i][j];
    }

    /** Swap two rows in the matrix */
    public Matrix swapRow(int r1, int r2){
        double tempRow[] = new double[rows];
        Matrix returnMatrix = new Matrix(rows,cols,M);
        
        if( (r1 >= rows) | (r2 >= rows) | (r1 < 0) | (r2 < 0) )
            throw new ArithmeticException("Matrix.swapRow: r1 or r2 not within matrix: " + r1 + ","+ r2 );
        
        tempRow = returnMatrix.M[r1];
        returnMatrix.M[r1] = returnMatrix.M[r2];
        returnMatrix.M[r2] = tempRow;
        
        return returnMatrix;
        
    }
        
    public Matrix mulRow(int r1,double scalar){
        Matrix returnMatrix = new Matrix(rows,cols,M);
        
        if( (r1 >= rows) || (r1 < 0) )
            throw new ArithmeticException("Matrix.mulRow: r1 not within matrix: " + r1);
        
        for(int i=0; i<cols; i++)
            returnMatrix.M[r1][i] = M[r1][i] * scalar;
        
        return returnMatrix;
    }
    
    
    /** Add a new row to the matrix */
    public Matrix addMulRow(int r1, int r2, double scalar){
        Matrix returnMatrix = new Matrix(rows,cols,M);
        Matrix tempMatrix = new Matrix(rows,cols,M);
        
        if( (r1 >= rows) | (r2 >= rows) | (r1 < 0) | (r2 < 0) )
            throw new ArithmeticException("Matrix.addMulRow: r1 or r2 not within matrix: " + r1 + ","+r2);
        
        tempMatrix = tempMatrix.mulRow(r1,scalar);
        
        for(int i=0; i<cols; i++)
            returnMatrix.M[r2][i] = returnMatrix.M[r2][i] + tempMatrix.M[r1][i];
        
        return returnMatrix;
    }
    
    
    /** Perform an elimination on the matrix to solve */
    public Matrix gaussJord(){
        Matrix ret = new Matrix(rows,cols,M);
        
        for(int i=0;i<rows;i++){
            
            // first find first non-zero coefficient
            int j=0;
            while(ret.M[i][j] == (double)0){
                j++;
                if(j==cols)
                    break;
            }
            
            // if this row is all zeros just skip on to next row
            if(j==cols)
                continue;
            
            // get leading one
            ret = ret.mulRow( i, 1.0/ret.M[i][j]);
            
            //get zeros above leading one
            if(i!=0)
                for(int k=i-1; k>=0; k--)
                    ret=ret.addMulRow(i,k,-1.0*ret.M[k][j]);
            
            //get zeros below leading one
            
            for(int k=i+1; k<rows; k++)
                ret=ret.addMulRow(i,k,-1.0*ret.M[k][j]);
            
        } // end for loop
        
        // swap rows until all-zero rows are at bottom and all
        // leading one's descend to the right
        
        //first assign rank according to leading ones
        int rank[] = new int[rows];
        
        for(int i=0; i<rows; i++){
            int j=0;
            while(ret.M[i][j] == (double)0){
                j++;
                if(j==cols)
                    break;
            }
            rank[i] = j;
        }
        
        // then use bubble sort to put them in descending order
        int temp;
        for(int i=0; i<rows-1; i++){
            for(int j=i+1; j<rows;j++){
                if(rank[i] > rank[j]){
                    ret = ret.swapRow(j,i);
                    temp = rank[i];
                    rank[i] = rank[j];
                    rank[j] = temp;
                }
            }
        }
        
        //and finally
        return ret;
    }
    
    /** String representation of the matrix for debugging purposes */
    public String toString(){
        String string = "";
        for(int i=0;i<rows;i++){
            string += M[i][cols-1] + "   ";
            string += "\n";
        }
        return string;
    }
}
