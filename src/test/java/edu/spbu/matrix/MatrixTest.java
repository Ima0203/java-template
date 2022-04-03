package edu.spbu.matrix;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class MatrixTest
{
  /**
   * ожидается 4 таких теста
   */
  @Test
  public void mulDD() throws Exception {
    Matrix m1 = new DenseMatrix("m1.txt");
    Matrix m2 = new DenseMatrix("m2.txt");
    Matrix expected = new DenseMatrix("result.txt");
    Matrix result = m1.mul(m2);
    assertEquals(expected, result);
  }

  @Test
  public void mulDS() throws Exception {
    Matrix m1 = new DenseMatrix("m1.txt");
    Matrix m2 = new SparseMatrix("m2.txt");
    Matrix expected = new DenseMatrix("result.txt");
    assertEquals(expected, m1.mul(m2));
  }

  @Test
  public void mulSD() throws Exception {
    Matrix m1 = new SparseMatrix("m1.txt");
    Matrix m2 = new DenseMatrix("m2.txt");
    Matrix expected = new DenseMatrix("result.txt");
    Matrix result = m1.mul(m2);
    assertEquals(expected, result);
  }

  @Test
  public void mulSS() throws Exception {
    Matrix m1 = new SparseMatrix("m1.txt");
    Matrix m2 = new SparseMatrix("m2.txt");
    Matrix expected = new DenseMatrix("result.txt");
    assertEquals(expected, m1.mul(m2));
  }

  @Test
  public void dmulDD() throws Exception {
    Matrix m1 = new DenseMatrix("m1.txt");
    Matrix m2 = new DenseMatrix("m2.txt");
    Matrix expected = new DenseMatrix("result.txt");
    Matrix result = m1.dmul(m2);
    assertEquals(expected, result);
  }

  @Test
  public void dmulDS() throws Exception {
    Matrix m1 = new DenseMatrix("m1.txt");
    Matrix m2 = new SparseMatrix("m2.txt");
    Matrix expected = new DenseMatrix("result.txt");
    assertEquals(expected, m1.dmul(m2));
  }

  @Test
  public void dmulSD() throws Exception {
    Matrix m1 = new SparseMatrix("m1.txt");
    Matrix m2 = new DenseMatrix("m2.txt");
    Matrix expected = new DenseMatrix("result.txt");
    Matrix result = m1.dmul(m2);
    assertEquals(expected, result);
  }

  @Test
  public void dmulSS() throws Exception {
    Matrix m1 = new SparseMatrix("m1.txt");
    Matrix m2 = new SparseMatrix("m2.txt");
    Matrix expected = new DenseMatrix("result.txt");
    Matrix result = m1.dmul(m2);
    assertEquals(expected, result);
  }
}
