/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coders.quickbill;

import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.event.KeyEvent;
import static java.lang.Thread.sleep;
import javax.swing.JFrame;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import net.proteanit.sql.DbUtils;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignQuery;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.JasperViewer;

/**
 *
 * @author sunny
 */
public class ManageSales extends javax.swing.JFrame {
    Connection conn=null;
    ResultSet rs=null;
    PreparedStatement pst=null;
    /**
     * Creates new form ManageSales
     */
    public ManageSales() {
        initComponents();
        this.setIconImage(new ImageIcon(getClass().getResource("QB_Icon-32x32.png")).getImage());
        conn=javaconnect.ConnectDB();
    }
    //Program to set one instance of Sales Form
    private static ManageSales obj=null;
    public static ManageSales getObj(){
        if(obj==null){
            obj=new ManageSales();
        }
        return obj;
    }
    String dt="dd-MM-yyyy";
    String tm;
    public void CurrentDate(){   
        Thread clock;
        clock = new Thread(){
            @Override
            public void run(){
                for(;;){
                    Calendar cal = new GregorianCalendar();
                    int month=cal.get(Calendar.MONTH)+1;
                    int day=cal.get(Calendar.DAY_OF_MONTH);
                    int year=cal.get(Calendar.YEAR);
                    //ZoneId z = ZoneId.of( "America/Montreal" );
                    //ZonedDateTime zdt = ZonedDateTime.now( z );
                    //lblDate.setText("Date: "+day+"-"+month+"-"+year);
                    //dt=day+"-"+zdt.getMonthValue()+"-"+year;
                    //dt=year+"-"+zdt.getMonthValue()+"-"+day;
                    dt=year+"-"+month+"-"+day;
                    lblCurrentDate.setText("Date: "+dt);
                    
                    int second= cal.get(Calendar.SECOND);
                    int minute=cal.get(Calendar.MINUTE);
                    int hour=cal.get(Calendar.HOUR);
                    String AM_PM = cal.get(Calendar.AM_PM) == 0 ? "AM" : "PM";
                    //lblTime.setText("Time: "+hour+":"+minute+":"+second+" "+AM_PM);
                    tm=hour+":"+minute+":"+second+" "+AM_PM;
                    lblTime.setText("Time: "+tm);
                    try {
                        sleep(1000);
                    }
                    catch (InterruptedException ex) {
                        //Logger.getLogger(Product.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
        clock.start();  
    }
    //Program to GetBill ID
    int bid=0;
    public void getBillID(){
        try{
            String sql="Select * from Transactions";
            pst=conn.prepareStatement(sql);
            rs=pst.executeQuery();
            while(rs.next()){
                bid=rs.getInt("tid");
            }
            bid++;
            txtBillNo.setText(String.valueOf(bid));
            txtBarcode.setText("");
            txtBarcode.requestFocus();
            txtItem.setText("");
            txtQty.setText("0");
            txtUnit.setText("");
            txtRate.setText("0");
            txtTotal.setText("0");
            txtDiscount.setText("0");
            txtTaxValue.setText("0");
            txtCGSTPer.setText("0");
            txtCGSTAmt.setText("0");
            txtSGSTPer.setText("0");
            txtSGSTAmt.setText("0");
            txtIGSTPer.setText("0");
            txtIGSTAmt.setText("0");
            txtAmount.setText("0");
            txtAmount.setText("0");
            cmbPaymentType.setSelectedIndex(0);
            txtDescription.setText("");
            txtPrevBalance.setText("0");
            txtGrandTotal.setText("0");
            txtPaid.setText("0");
            txtBalance.setText("0");
            txtCustomerID.setText("");
            txtName.setText("");
            txtAddress.setText("");
            txtContact1.setText("");
            txtContact2.setText("");
            txtEmail.setText("");
            btnAdd.setEnabled(true);
            btnUpdate.setEnabled(false);
            btnDelete.setEnabled(false);
            btnUpdateTransaction.setEnabled(false);
            btnPrintInvoice.setEnabled(false);
            pst.close();
        }catch(SQLException e){
            JOptionPane.showMessageDialog(null, e,"getBillID() Exception",JOptionPane.ERROR_MESSAGE);
        }
    }
    //Program to get Previous Transaction details
    double bal;
    public void getPrevTrans(){
        try{
            String sql="Select t_balance from Transactions where t_customerID='"+txtCustomerID.getText()+"'";
            pst=conn.prepareStatement(sql);
            rs=pst.executeQuery();
            bal=0;
            while(rs.next()){
                bal=bal+rs.getDouble("t_balance");
            }
            txtPrevBalance.setText(Double.toString(bal));
            pst.close();
        }catch(SQLException e){
            JOptionPane.showMessageDialog(null, e,"getPrevTrans() Exception",JOptionPane.ERROR_MESSAGE);
        }
    }
    //Program to get Transaction details
    public void getTrans(){
        try{
            String sql="Select * from Transactions where t_invoiceID='"+txtBillNo.getText()+"'AND t_customerID='"+txtCustomerID.getText()+"'";
            pst=conn.prepareStatement(sql);
            rs=pst.executeQuery();
            if(rs.next()){
                btnSaveTransaction.setEnabled(false);
                btnUpdateTransaction.setEnabled(true);
                btnPrintInvoice.setEnabled(true);
            }
            else{
                btnSaveTransaction.setEnabled(true);
                btnUpdateTransaction.setEnabled(false);
                btnPrintInvoice.setEnabled(false);
            }
            pst.close();
        }catch(SQLException e){
            JOptionPane.showMessageDialog(null, e,"getTrans() Exception",JOptionPane.ERROR_MESSAGE);
        }
    }
    //Program to save transaction
    public void saveTrans(){
        try{
            String sql="Insert into Transactions (t_date,t_invoiceID,t_customerID,t_name,t_address,t_contact1,t_contact2,t_email,t_tot_cgst,t_tot_sgst,"
                    + "t_tot_igst,t_paymentType,t_description,t_grandTotal,t_paid,t_balance)values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            pst=conn.prepareStatement(sql);
            pst.setString(1, dt);
            pst.setString(2, txtBillNo.getText());
            pst.setString(3, txtCustomerID.getText());
            pst.setString(4, txtName.getText());
            pst.setString(5, txtAddress.getText());
            pst.setString(6, txtContact1.getText());
            pst.setString(7, txtContact2.getText());
            pst.setString(8, txtEmail.getText());
            pst.setDouble(9, totcgst);
            pst.setDouble(10, totsgst);
            pst.setDouble(11, totigst);
            pst.setString(12, cmbPaymentType.getSelectedItem().toString());
            pst.setString(13, txtDescription.getText());
            pst.setString(14, txtGrandTotal.getText());
            pst.setString(15, txtPaid.getText());
            pst.setString(16, txtBalance.getText());
            pst.execute();
            pst.close();
            JOptionPane.showMessageDialog(null, "Transaction saved successfully","Trasaction Saved",JOptionPane.INFORMATION_MESSAGE);
        }catch(SQLException e){
            JOptionPane.showMessageDialog(null, e,"saveTrans() Exception",JOptionPane.ERROR_MESSAGE);
        }
    }
    //Program to load Product on Barcode scan
    double qty,rate,ttl,disc,discAmt,taxval,taxAmt,cgst,cgstPer,csgtAmt,sgst,sgstPer,sgstAmt,igst,igstPer,igstAmt,FinalAmt;
    String item;
    String HSN;
    double newQty;
    public void barcodeScan(){
        try{
            String sql="Select * from Products where p_barcode='"+txtBarcode.getText()+"'";
            pst=conn.prepareStatement(sql);
            rs=pst.executeQuery();
            if(rs.next()){
                qty=rs.getDouble("p_qty");
                maxqty=qty;
                if(qty>=1){
                    qty=1;
                    txtItem.setText(rs.getString("p_itemName"));
                    HSN=rs.getString("p_hsncode");
                    txtQty.setText(Double.toString(qty));
                    txtUnit.setText(rs.getString("p_unit"));
                    txtRate.setText(rs.getString("p_rate"));
                    rate=rs.getDouble("p_rate");
                    ttl=qty*rate;
                    txtTotal.setText(Double.toString(ttl));
                    discAmt=(disc*ttl)/100;
                    txtDiscount.setText(Double.toString(discAmt));
                    taxval=discAmt+ttl;
                    txtTaxValue.setText(Double.toString(taxval));
                    //Code to calculated CGST amount
                    txtCGSTPer.setText(rs.getString("p_cgst"));
                    cgstPer=rs.getDouble("p_cgst");
                    cgst=(cgstPer*taxval)/100;                
                    txtCGSTAmt.setText(Double.toString(cgst));
                    //Code to calculated SGST amount
                    txtSGSTPer.setText(rs.getString("p_sgst"));
                    sgstPer=rs.getDouble("p_sgst");
                    sgst=(sgstPer*taxval)/100;
                    txtSGSTAmt.setText(Double.toString(sgst));
                    //Code to calculate IGST
                    txtIGSTPer.setText(rs.getString("p_igst"));
                    igstPer=rs.getDouble("p_igst");
                    igst=(igstPer*taxval)/100;
                    txtIGSTAmt.setText(Double.toString(igst));
                    //Code to calculate final amount
                    FinalAmt=taxval+cgst+sgst+igst;
                    txtAmount.setText(Double.toString(FinalAmt));
                    btnAdd.setEnabled(true);
                    btnUpdate.setEnabled(false);
                    btnDelete.setEnabled(false);
                    addToBill();
                    getBillData();
                    finalCalcAdd();
                }
                else{
                    JOptionPane.showMessageDialog(null, "Scanned product is out of stock","Out of stock",JOptionPane.ERROR_MESSAGE);
                }                                              
            }
            else{
                txtBarcode.setText("");  
                HSN="";
                txtItem.setText("");
                txtQty.setText("0");
                txtUnit.setText(" ");
                txtRate.setText("0");
                txtTotal.setText("0");
                txtDiscount.setText("0");
                txtTaxValue.setText("0");
                txtCGSTPer.setText("0");
                txtCGSTAmt.setText("0");
                txtSGSTPer.setText("0");
                txtSGSTAmt.setText("0");
                txtIGSTPer.setText("0");
                txtIGSTAmt.setText("0");
                txtAmount.setText("0");
                btnAdd.setEnabled(false);
                btnUpdate.setEnabled(false);
                btnDelete.setEnabled(false);  
                txtBarcode.requestFocus();
                JOptionPane.showMessageDialog(null, "Product not found in record","Oop's! Record not found",JOptionPane.WARNING_MESSAGE);                
            }
            
        pst.close();
        }
        catch(SQLException e){
            JOptionPane.showMessageDialog(null, e,"barcodescan() Exception",JOptionPane.ERROR_MESSAGE);
        }
    }
    //Program to get Item List from data
    public void findProduct(){
        try{
            String KeyVal=txtItem.getText();
            String sql="Select pid as 'Product ID',p_barcode as 'Barcode',p_hsncode as 'HSN Code',p_itemName as 'Item Name',p_description as 'Description',"
                    + "p_qty as 'Quantity',p_unit as 'Unit',p_rate as 'Rate',p_cgst as 'CGST',p_sgst as 'SGST',p_igst as 'IGST' from Products where p_itemName LIKE ?";
            pst=conn.prepareStatement(sql);
            pst.setString(1,"%"+KeyVal + "%");
            rs=pst.executeQuery();
            tblProducts.setModel(DbUtils.resultSetToTableModel(rs));
            pst.close();
        }
        catch(SQLException e){
            JOptionPane.showMessageDialog(null, e,"findProduct() Exception",JOptionPane.ERROR_MESSAGE);
        }
    }
    //Program to load selected product to fields
    double maxqty=0;
    public void loadSelect(){
        try{            
            txtBarcode.setText(tblProducts.getValueAt(0, 1).toString());
            HSN=tblProducts.getValueAt(0, 2).toString();
            txtItem.setText(tblProducts.getValueAt(0, 3).toString());
            txtQty.setText(Double.toString(qty));
            maxqty=Double.parseDouble(tblProducts.getValueAt(0, 5).toString());
            txtUnit.setText(tblProducts.getValueAt(0, 6).toString());
            txtRate.setText(tblProducts.getValueAt(0, 7).toString());
            txtCGSTPer.setText(tblProducts.getValueAt(0, 8).toString());
            txtSGSTPer.setText(tblProducts.getValueAt(0, 9).toString());
            txtIGSTPer.setText(tblProducts.getValueAt(0, 10).toString());
        }
        catch(NumberFormatException e){
            JOptionPane.showMessageDialog(null, e);
        }
    }
    //Program to calculate field values
    double qty1,rate1,tot1,disc1=0,taxVal1=0,cgstPer1=0,cgstAmt1=0,sgstPer1=0,sgstAmt1=0,igstPer1=0,igstAmt1=0,grossAmt1=0;
    public void calcFieldVal(){
        try{
            cgstPer1=0;
            sgstPer1=0;
            igstPer1=0;
            qty1=Double.parseDouble(txtQty.getText());
            rate1=Double.parseDouble(txtRate.getText());
            tot1=qty1*rate1;
            txtTotal.setText(Double.toString(tot1));
            disc1=Double.valueOf(txtDiscount.getText());
            
            taxVal1=tot1-((tot1*disc1)/100);
            txtTaxValue.setText(Double.toString(taxVal1));
            
            cgstAmt1=(Double.parseDouble(txtCGSTPer.getText())*taxVal1)/100;
            txtCGSTAmt.setText(Double.toString(cgstAmt1));
            
            sgstAmt1=(Double.parseDouble(txtSGSTPer.getText())*taxVal1)/100;
            txtSGSTAmt.setText(Double.toString(sgstAmt1));
            
            igstAmt1=(Double.parseDouble(txtIGSTPer.getText())*taxVal1)/100;
            txtIGSTAmt.setText(Double.toString(igstAmt1));
            
            grossAmt1=taxVal1+cgstAmt1+sgstAmt1+igstAmt1;
            txtAmount.setText(Double.toString(grossAmt1));
        }catch(NumberFormatException e){
            JOptionPane.showMessageDialog(null, e,"calcField() Exception",JOptionPane.ERROR_MESSAGE);
        }
    }
    //Program to getCustomer Details on Name Entry
    public void getCustDataToTblOnName(){
        try{
            String KeyVal=txtName.getText();
            String sql="Select cid as 'Customer ID',cName as 'Name',cAddress as 'Address',cContact as 'Contact-1',cContact2 as 'Contact-2',cEmail as 'Email' from Customer where cName LIKE ?";
            pst=conn.prepareStatement(sql);
            pst.setString(1,"%"+KeyVal + "%");
            rs=pst.executeQuery();
            tblCustomers.setModel(DbUtils.resultSetToTableModel(rs));
            pst.close();
        }
        catch(SQLException e){
            JOptionPane.showMessageDialog(null, e,"getCustDataToTblOnName() Exception",JOptionPane.ERROR_MESSAGE);
        }
    }
    //Program to getCustomer Details on Address Entry
    public void getCustDataToTblOnAdrs(){
        try{
            String KeyVal=txtAddress.getText();
            String sql="Select cid as 'Customer ID',cName as 'Name',cAddress as 'Address',cContact as 'Contact-1',cContact2 as 'Contact-2',cEmail as 'Email' from Customer where cAddress LIKE ?";
            pst=conn.prepareStatement(sql);
            pst.setString(1,"%"+KeyVal + "%");
            rs=pst.executeQuery();
            tblCustomers.setModel(DbUtils.resultSetToTableModel(rs));
            pst.close();
        }
        catch(SQLException e){
            JOptionPane.showMessageDialog(null, e,"getCustDataToTblOnAdrs() Exception",JOptionPane.ERROR_MESSAGE);
        }
    }
    //Program to getCustomer Details on Contact1 Entry
    public void getCustDataToTblOnCon1(){
        try{
            String KeyVal=txtContact1.getText();
            String sql="Select cid as 'Customer ID',cName as 'Name',cAddress as 'Address',cContact as 'Contact-1',cContact2 as 'Contact-2',cEmail as 'Email' from Customer where cContact LIKE ?";
            pst=conn.prepareStatement(sql);
            pst.setString(1,"%"+KeyVal + "%");
            rs=pst.executeQuery();
            tblCustomers.setModel(DbUtils.resultSetToTableModel(rs));
            pst.close();
        }
        catch(SQLException e){
            JOptionPane.showMessageDialog(null, e,"getCustDataToTblOnCon1() Exception",JOptionPane.ERROR_MESSAGE);
        }
    }
    //Program to getCustomer Details on Contact2 Entry
    public void getCustDataToTblOnCon2(){
        try{
            String KeyVal=txtName.getText();
            String sql="Select cid as 'Customer ID',cName as 'Name',cAddress as 'Address',cContact as 'Contact-1',cContact2 as 'Contact-2',"
                    + "cEmail as 'Email' from Customer where cContact2 LIKE ?";
            pst=conn.prepareStatement(sql);
            pst.setString(1,"%"+KeyVal + "%");
            rs=pst.executeQuery();
            tblCustomers.setModel(DbUtils.resultSetToTableModel(rs));
            pst.close();
        }
        catch(SQLException e){
            JOptionPane.showMessageDialog(null, e,"getCustDataToTblOnCon2() Exception",JOptionPane.ERROR_MESSAGE);
        }
    }
    //Program to getCustomer Details on Email Entry
    public void getCustDataToTblOnEmail(){
        try{
            String KeyVal=txtEmail.getText();
            String sql="Select cid as 'Customer ID',cName as 'Name',cAddress as 'Address',cContact as 'Contact-1',cContact2 as 'Contact-2',"
                    + "cEmail as 'Email' from Customer where cEmail LIKE ?";
            pst=conn.prepareStatement(sql);
            pst.setString(1,"%"+KeyVal + "%");
            rs=pst.executeQuery();
            tblCustomers.setModel(DbUtils.resultSetToTableModel(rs));
            pst.close();
        }
        catch(SQLException e){
            JOptionPane.showMessageDialog(null, e,"getCustDataToTblOnEmail() Exception",JOptionPane.ERROR_MESSAGE);
        }
    }
    //Program to load selected product to fields
    public void loadSelectCust(){        
        try{            
            row=0;
            String cid=tblCustomers.getValueAt(row, 0).toString();
            String sql="Select * from Customer where cid='"+cid+"'";
            pst=conn.prepareStatement(sql);
            rs=pst.executeQuery();
            if(rs.next()){
                txtCustomerID.setText(rs.getString("cid"));
                txtName.setText(rs.getString("cName"));
                txtAddress.setText(rs.getString("cAddress"));
                txtContact1.setText(rs.getString("cContact"));
                txtContact2.setText(rs.getString("cContact2"));
                txtEmail.setText(rs.getString("cEmail"));
            }
            pst.close();
        }
        catch(SQLException e){
            JOptionPane.showMessageDialog(null, e,"loadSelectCust() Exception",JOptionPane.ERROR_MESSAGE);
        }
    }
    //Program to reset Customer and Transaction Fields
    public void resetCustTrans(){
        txtCustomerID.setText("");
        txtName.setText("");
        txtAddress.setText("");
        txtContact1.setText("");
        txtContact2.setText("");
        txtEmail.setText("");
        tblCustomers.removeAll();
        txtDescription.setText("");
        txtPrevBalance.setText("");
        txtGrandTotal.setText("");
        txtPaid.setText("");
        txtBalance.setText("");
        btnSaveTransaction.setEnabled(true);
        btnUpdateTransaction.setEnabled(false);
        btnPrintInvoice.setEnabled(false);
    }
    //Program to reset fields
    public void resetFields(){
        txtBarcode.setText("");
        txtItem.setText("");
        txtQty.setText("0");
        txtUnit.setText("");
        txtRate.setText("0");
        txtTotal.setText("0");
        txtDiscount.setText("0");
        txtTaxValue.setText("0");
        txtCGSTPer.setText("0");
        txtCGSTAmt.setText("0");
        txtSGSTPer.setText("0");
        txtSGSTAmt.setText("0");
        txtIGSTPer.setText("0");
        txtIGSTAmt.setText("0");
        txtAmount.setText("0");
        txtBarcode.requestFocus();
        btnAdd.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
    }
    //Program to Add Product to Bill List
    public void addToBill(){
        try{
            String sql="Insert into Sales(s_InvoiceID,s_CustomerID,s_Barcode,s_Date,s_Item,s_Qty,s_Unit,s_Rate,s_Total,s_Discount,s_TaxableValue,"
                    + "s_CGSTPer,s_CGSTAmt,s_SGSTPer,s_SGSTAmt,s_IGSTPer,s_IGSTAmt,s_Amount,s_HSNCode)"
                    + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                pst=conn.prepareStatement(sql);
                pst.setString(1, txtBillNo.getText());
                pst.setString(2, txtCustomerID.getText());
                pst.setString(3, txtBarcode.getText());
                pst.setString(4, dt);
                pst.setString(5, txtItem.getText());
                pst.setString(6, txtQty.getText());
                newQty=maxqty-Double.parseDouble(txtQty.getText());//Code to assign new stock value
                pst.setString(7, txtUnit.getText());
                pst.setString(8, txtRate.getText());
                pst.setString(9, txtTotal.getText());
                pst.setString(10, txtDiscount.getText());
                pst.setString(11, txtTaxValue.getText());
                pst.setString(12, txtCGSTPer.getText());
                pst.setString(13, txtCGSTAmt.getText());
                pst.setString(14, txtSGSTPer.getText());
                pst.setString(15, txtSGSTAmt.getText());
                pst.setString(16, txtIGSTPer.getText());
                pst.setString(17, txtIGSTAmt.getText());
                pst.setString(18, txtAmount.getText());
                pst.setInt(19, Integer.parseInt(HSN));
                pst.execute();
                pst.close();
        }catch(HeadlessException | SQLException e){
            JOptionPane.showMessageDialog(null, e,"addToBill() Exception",JOptionPane.ERROR_MESSAGE);
        }
        try{
            String sql="Update Products set p_qty=? where p_itemName='"+txtItem.getText()+"'";
            pst=conn.prepareStatement(sql);
            pst.setDouble(1, newQty);
            pst.execute();
            pst.close();
        }catch(SQLException e){
            JOptionPane.showMessageDialog(null, e,"addToBill() Exception",JOptionPane.ERROR_MESSAGE);
        }
    }
    //Program to Display Bill Data on Table
    public void getBillData(){
        try{
            String sql="Select s_count as 'Sales Count',s_InvoiceID as 'Bill No.',s_CustomerID as 'Customer ID',s_Barcode as 'Barcode',s_HSNCode as 'HSN Code',s_Date as 'Date',s_Item as 'Item Name',"
                    + "s_Qty as 'Quantity',s_Unit as 'Unit',s_Rate as 'Rate',s_Total as 'Total',s_Discount as 'Discount',s_TaxableValue as 'Taxable Amount',s_CGSTPer as 'CGST Rate',"
                    + "s_CGSTAmt as 'CGST Amt',s_SGSTPer as 'SGST Rate',s_SGSTAmt as 'SGST Amt',s_IGSTPer as 'IGST Rate',s_IGSTAmt as 'SGST Amt',"
                    + "s_Amount as 'Total' from Sales where s_InvoiceID='"+txtBillNo.getText()+"'";
            pst=conn.prepareStatement(sql);
            rs=pst.executeQuery();
            tblBilllingList.setModel(DbUtils.resultSetToTableModel(rs));
            pst.close();            
        }catch(SQLException e){
            JOptionPane.showMessageDialog(null, e,"getBillData() Exception",JOptionPane.ERROR_MESSAGE);
        }
    }
    //Program to perform Final Amount Calculations on Add Button Press
    double totcgst,totsgst,totigst;
    public void finalCalcAdd(){        
        try{        
            double AMOUNT=0,dbAMT=0;
            totcgst=0;totsgst=0;totigst=0;
            String sql="Select * from Sales where s_InvoiceID='"+txtBillNo.getText()+"'";
            pst=conn.prepareStatement(sql);
            rs=pst.executeQuery();
            while(rs.next()){
                dbAMT=rs.getDouble("s_Amount");
                AMOUNT=Math.round(AMOUNT+dbAMT);
                totcgst=totcgst+rs.getDouble("s_CGSTAmt");
                totsgst=totsgst+rs.getDouble("s_SGSTAmt");
                totigst=totigst+rs.getDouble("s_IGSTAmt");
            }
            txtGrandTotal.setText(Double.toString(AMOUNT));
            txtBalance.setText(Double.toString(AMOUNT));
            pst.close();
            
        }catch(SQLException e){
            JOptionPane.showMessageDialog(null, e);
        }
    }
    
    //Program to readSalesDB on Manual Bill Number Entry
    public void readSalesOnBillNoEntry(){
        try{
            if(!"".equals(txtBillNo.getText())){
                getBillData();
            }            
        }
        catch(Exception e){
            JOptionPane.showMessageDialog(null, e,"readSalesOnBillNoEntry() Exception",JOptionPane.ERROR_MESSAGE);
        }
    }
    //Program to read Transaction database
    public void readTrans(){
        try{
            String sql="Select * from Transactions where t_invoiceID='"+txtBillNo.getText()+"'";
            pst=conn.prepareStatement(sql);
            rs=pst.executeQuery();
            if(rs.next()){
                txtCustomerID.setText(rs.getString("t_customerID"));
                txtName.setText(rs.getString("t_name"));
                txtAddress.setText(rs.getString("t_address"));
                txtContact1.setText(rs.getString("t_contact1"));
                txtContact2.setText(rs.getString("t_contact2"));
                txtEmail.setText(rs.getString("t_email"));
                //lblDate.setText("Date:"+rs.getString("t_date"));
                cmbPaymentType.setSelectedItem(rs.getString("t_paymentType"));
                txtDescription.setText(rs.getString("t_description"));
                txtGrandTotal.setText(rs.getString("t_grandTotal"));
                txtPaid.setText(rs.getString("t_paid"));
                txtBalance.setText(rs.getString("t_balance"));
                btnSaveTransaction.setEnabled(false);
                btnUpdateTransaction.setEnabled(true);
                btnPrintInvoice.setEnabled(true);
            }
            pst.close();
        }
        catch(SQLException e){
            JOptionPane.showMessageDialog(null, e,"readTrans() Exception",JOptionPane.ERROR_MESSAGE);
        }
    }
    //Program to get Billing table data to fields
    String count,itemString;
    int row;
    double currentSalesQty,newSalesQty,oldSalesQty;
    public void getBillingDataToField(){
        try{
            row=tblBilllingList.getSelectedRow();
            String tblClick=tblBilllingList.getModel().getValueAt(row, 0).toString();
            count=tblBilllingList.getModel().getValueAt(row, 0).toString();            
            String sql="Select * from Sales where s_count='"+tblClick+"'";
            pst=conn.prepareStatement(sql);
            rs=pst.executeQuery();
            if(rs.next()){
                txtBarcode.setText(rs.getString("s_Barcode"));
                HSN=rs.getString("s_HSNCode");
                txtItem.setText(rs.getString("s_Item"));
                txtQty.setText(rs.getString("s_Qty"));
                oldSalesQty=Double.parseDouble(rs.getString("s_Qty"));
                txtUnit.setText(rs.getString("s_Unit"));
                txtRate.setText(rs.getString("s_Rate"));
                txtTotal.setText(rs.getString("s_Total"));
                txtDiscount.setText(rs.getString("s_Discount"));
                txtTaxValue.setText(rs.getString("s_TaxableValue"));
                txtCGSTPer.setText(rs.getString("s_CGSTPer"));
                txtCGSTAmt.setText(rs.getString("s_CGSTAmt"));
                txtSGSTPer.setText(rs.getString("s_SGSTPer"));
                txtSGSTAmt.setText(rs.getString("s_SGSTAmt"));
                txtIGSTPer.setText(rs.getString("s_IGSTPer"));
                txtIGSTAmt.setText(rs.getString("s_IGSTAmt"));
                txtAmount.setText(rs.getString("s_Amount"));
                btnAdd.setEnabled(false);
                btnUpdate.setEnabled(true);
                btnDelete.setEnabled(true);
            }
            pst.close();
        }
        catch(SQLException e){
            JOptionPane.showMessageDialog(null, e);
        }        
    }
    //Program to update billing list
    double currentQty,pQty;
    public void updtBillList(){
        try{
            row=tblBilllingList.getSelectedRow();
            String tblClick=tblBilllingList.getModel().getValueAt(row, 0).toString();
            String sql="update Sales set s_Barcode=?,s_Item=?,s_Qty=?,s_Unit=?,s_Rate=?,s_Total=?,s_Discount=?,s_TaxableValue=?,"
                    + "s_CGSTPer=?,s_CGSTAmt=?,s_SGSTPer=?,s_SGSTAmt=?,s_IGSTPer=?,s_IGSTAmt=?,s_Amount=?,s_HSNCode=? where s_count='"+tblClick+"'";
            pst=conn.prepareStatement(sql);
            pst.setString(1, txtBarcode.getText());
            pst.setString(2, txtItem.getText());
            pst.setString(3, txtQty.getText());
            newSalesQty=Double.parseDouble(txtQty.getText());
            pst.setString(4, txtUnit.getText());
            pst.setString(5, txtRate.getText());
            pst.setString(6, txtTotal.getText());
            pst.setString(7, txtDiscount.getText());
            pst.setString(8, txtTaxValue.getText());
            pst.setString(9, txtCGSTPer.getText());
            pst.setString(10, txtCGSTAmt.getText());
            pst.setString(11, txtSGSTPer.getText());
            pst.setString(12, txtSGSTAmt.getText());
            pst.setString(13, txtIGSTPer.getText());
            pst.setString(14, txtIGSTAmt.getText());
            pst.setString(15, txtAmount.getText());
            pst.setString(16, HSN);
            pst.execute();
            pst.close();
            getBillData();
        }
        catch(SQLException e){
            JOptionPane.showMessageDialog(null, e);
        }
        //Code to get current stock qty from Products        
        try{
            String itemName=tblBilllingList.getModel().getValueAt(row, 5).toString();
            String sql="Select p_qty from Products where p_itemName='"+itemName+"'";
            pst=conn.prepareStatement(sql);
            rs=pst.executeQuery();
            if(rs.next()){
                currentQty=rs.getDouble("p_qty");
            }
            pst.close();
        }
        catch(SQLException e){
            JOptionPane.showMessageDialog(null, e,"Exception while retrieving data",JOptionPane.ERROR_MESSAGE);
        }
        //Code tp update stock qty
        try{
            if(newSalesQty>oldSalesQty && newSalesQty!=oldSalesQty){
                currentSalesQty=newSalesQty-oldSalesQty;
                pQty=currentQty-currentSalesQty;
            }
            else if(newSalesQty<oldSalesQty && newSalesQty!=oldSalesQty){
                currentSalesQty=oldSalesQty-newSalesQty;
                pQty=currentQty+currentSalesQty;
            }
            String itemName=tblBilllingList.getModel().getValueAt(row, 5).toString();
            String sql="Update Products set p_qty='"+pQty+"' where p_itemName='"+itemName+"'";
            pst=conn.prepareStatement(sql);
            pst.execute();
            pst.close();
        }
        catch(SQLException e){
            JOptionPane.showMessageDialog(null, e);
        }
    }
    //Program to Delete Data from Sales Database
    public void delBillData(){
        double delQty=Double.parseDouble(txtQty.getText());
        //Code to get current stock qty from Products        
        try{
            String itemName=tblBilllingList.getModel().getValueAt(row, 5).toString();
            String sql="Select p_qty from Products where p_itemName='"+itemName+"'";
            pst=conn.prepareStatement(sql);
            rs=pst.executeQuery();
            if(rs.next()){
                currentQty=rs.getDouble("p_qty");
            }
            pst.close();
        }
        catch(SQLException e){
            JOptionPane.showMessageDialog(null, e,"Exception while retrieving data",JOptionPane.ERROR_MESSAGE);
        }
        //Code tp update stock qty
        try{
            pQty=currentQty+delQty;
            String itemName=tblBilllingList.getModel().getValueAt(row, 5).toString();
            String sql="Update Products set p_qty='"+pQty+"' where p_itemName='"+itemName+"'";
            pst=conn.prepareStatement(sql);
            pst.execute();
            //JOptionPane.showMessageDialog(null, "Product quantity updated.");
            pst.close();
        }
        catch(SQLException e){
            JOptionPane.showMessageDialog(null, e);
        }
        //Program to delete stock from Sales Database
        try{
            String sql="Delete from Sales where s_count='"+count+"'";
            pst=conn.prepareStatement(sql);
            pst.execute();
            //JOptionPane.showMessageDialog(null,"Data deleted from database.");
            resetFields();
            getBillData();
            pst.close();
        }
        catch(HeadlessException | SQLException e){
            JOptionPane.showMessageDialog(null, "Item removed from Sales.","Item removed successfully",JOptionPane.INFORMATION_MESSAGE);
        }        
    }
    //Program to load data on mouse click on table
    public void loadSelectCustTbl(){        
        try{            
            row=tblCustomers.getSelectedRow();
            String cid=tblCustomers.getValueAt(row, 0).toString();
            String sql="Select * from Customer where cid='"+cid+"'";
            pst=conn.prepareStatement(sql);
            rs=pst.executeQuery();
            if(rs.next()){
                txtCustomerID.setText(rs.getString("cid"));
                txtName.setText(rs.getString("cName"));
                txtAddress.setText(rs.getString("cAddress"));
                txtContact1.setText(rs.getString("cContact"));
                txtContact2.setText(rs.getString("cContact2"));
                txtEmail.setText(rs.getString("cEmail"));
            }
            pst.close();
        }
        catch(SQLException e){
            JOptionPane.showMessageDialog(null, e,"loadSelectCust() Exception",JOptionPane.ERROR_MESSAGE);
        }
    }
    //Program to perform calculation on txtPaid field entry
    double PreBal=0,GrandTtl,Paid,BAL;
    public void calcOnPaid(){        
        try{
            PreBal=Double.parseDouble(txtPrevBalance.getText());
            GrandTtl=Double.parseDouble(txtGrandTotal.getText());
            Paid=Double.parseDouble(txtPaid.getText());
            BAL=GrandTtl-Paid;
            txtBalance.setText(Double.toString(BAL));
        }
        catch(NumberFormatException e){
            JOptionPane.showMessageDialog(null, e,"calcOnPaid() Exception",JOptionPane.ERROR_MESSAGE);
        }
    }
    //Program to update transaction
    public void updtTrans(){
        try{
            String sql="Update Transactions set t_tot_cgst=?,t_tot_sgst=?,t_tot_igst=?,t_paymentType=?,t_description=?,t_grandTotal=?,t_paid=?,"
                    + "t_balance=? where t_invoiceID='"+txtBillNo.getText()+"' AND t_customerID='"+txtCustomerID.getText()+"'";
            pst=conn.prepareStatement(sql);
            pst.setDouble(1, totcgst);
            pst.setDouble(2, totsgst);
            pst.setDouble(3, totigst);
            pst.setString(4, cmbPaymentType.getSelectedItem().toString());
            pst.setString(5, txtDescription.getText());
            pst.setString(6, txtGrandTotal.getText());
            pst.setString(7, txtPaid.getText());
            pst.setString(8, txtBalance.getText());
            pst.execute();
            pst.close();
            JOptionPane.showMessageDialog(null, "Transaction saved updated","Trasaction Updated",JOptionPane.INFORMATION_MESSAGE);
        }catch(SQLException e){
            JOptionPane.showMessageDialog(null, e,"updtTrans() Exception",JOptionPane.ERROR_MESSAGE);
        }
    }
    //Program to give printout options
    //final JDialog dialog=new JDialog();
    public void printOptions(){
        try{
            String CustID=txtCustomerID.getText(),InvID=txtBillNo.getText();
            String[] choice={"CGST+SSGT Invoice","IGST Invoice"};
            //Integer[] options = {1, 3, 5, 7, 9, 11};
            //Double[] options = {3.141, 1.618};
            //Character[] options = {'a', 'b', 'c', 'd'};
            
            int x = JOptionPane.showOptionDialog(null, "Choose Print Method","Invoice Print",JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, choice, choice[0]);
            //System.out.println(x);
            //JOptionPane.showMessageDialog(null, "You Selected: "+x);            
            if(x == 0){
                try{
                    String sql="Select * from Transactions,Sales,Company where Sales.s_InvoiceID='"+InvID+"' "
                            + "AND Transactions.t_invoiceID='"+InvID+"'";
                    JasperDesign jd= JRXmlLoader.load("src/coders/reports/a4Invoice2.jrxml");
                    JRDesignQuery qry=new JRDesignQuery();
                    qry.setText(sql);
                    jd.setQuery(qry);
                    JasperReport jr= JasperCompileManager.compileReport(jd);
                    JasperPrint jp=JasperFillManager.fillReport(jr, null,conn);
                    JasperViewer.viewReport(jp,false);
                }catch(JRException e){
                    JOptionPane.showMessageDialog(null, e,"printOption() Exception",JOptionPane.ERROR_MESSAGE);
                }
            }
            else{
                try{
                    String sql="Select * from Transactions,Sales,Company where Sales.s_InvoiceID='"+InvID+"' "
                            + "AND Transactions.t_invoiceID='"+InvID+"'";
                    JasperDesign jd= JRXmlLoader.load("src/coders/reports/a4Invoice3.jrxml");
                    JRDesignQuery qry=new JRDesignQuery();
                    qry.setText(sql);
                    jd.setQuery(qry);
                    JasperReport jr= JasperCompileManager.compileReport(jd);
                    JasperPrint jp=JasperFillManager.fillReport(jr, null,conn);
                    JasperViewer.viewReport(jp,false);
                }catch(JRException e){
                    JOptionPane.showMessageDialog(null, e,"printOption() Exception",JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        catch(HeadlessException e){
            JOptionPane.showMessageDialog(null, e,"Print Invoice Exception",JOptionPane.ERROR_MESSAGE);
        }
    }    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtCustomerID = new javax.swing.JTextField();
        txtName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtAddress = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtContact1 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtContact2 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblCustomers = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        txtBillNo = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtBarcode = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtItem = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtQty = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        txtRate = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        txtTotal = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        txtDiscount = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        txtTaxValue = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        txtAmount = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblProducts = new javax.swing.JTable();
        btnAdd = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        cmbPaymentType = new javax.swing.JComboBox<>();
        jLabel17 = new javax.swing.JLabel();
        txtDescription = new javax.swing.JTextField();
        txtGrandTotal = new javax.swing.JTextField();
        txtPaid = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        txtBalance = new javax.swing.JTextField();
        btnSaveTransaction = new javax.swing.JButton();
        btnUpdateTransaction = new javax.swing.JButton();
        btnPrintInvoice = new javax.swing.JButton();
        txtUnit = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        txtPrevBalance = new javax.swing.JTextField();
        lblCurrentDate = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        txtSGSTPer = new javax.swing.JTextField();
        txtCGSTAmt = new javax.swing.JTextField();
        txtCGSTPer = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        txtSGSTAmt = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        txtIGSTAmt = new javax.swing.JTextField();
        txtIGSTPer = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblBilllingList = new javax.swing.JTable();
        lblTime = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        mnuManageCustomer = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Sales Manager - Coders QuickBill 2017");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(40, 40, 40));

        jPanel2.setBackground(new java.awt.Color(1, 23, 82));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Customer Details", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 12), java.awt.Color.white)); // NOI18N

        jLabel1.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        jLabel1.setForeground(java.awt.Color.white);
        jLabel1.setText("Customer ID");

        txtCustomerID.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        txtCustomerID.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtCustomerIDFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtCustomerIDFocusLost(evt);
            }
        });

        txtName.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        txtName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtNameFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtNameFocusLost(evt);
            }
        });
        txtName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtNameKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtNameKeyReleased(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        jLabel2.setForeground(java.awt.Color.white);
        jLabel2.setText("Name");

        jLabel3.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        jLabel3.setForeground(java.awt.Color.white);
        jLabel3.setText("Address");

        txtAddress.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        txtAddress.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtAddressFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtAddressFocusLost(evt);
            }
        });
        txtAddress.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtAddressKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtAddressKeyReleased(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        jLabel4.setForeground(java.awt.Color.white);
        jLabel4.setText("Contact-1");

        txtContact1.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        txtContact1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtContact1FocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtContact1FocusLost(evt);
            }
        });
        txtContact1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtContact1KeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtContact1KeyReleased(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        jLabel5.setForeground(java.awt.Color.white);
        jLabel5.setText("Contact-2");

        txtContact2.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        txtContact2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtContact2FocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtContact2FocusLost(evt);
            }
        });
        txtContact2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtContact2KeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtContact2KeyReleased(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        jLabel6.setForeground(java.awt.Color.white);
        jLabel6.setText("E-mail");

        txtEmail.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        txtEmail.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtEmailFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtEmailFocusLost(evt);
            }
        });
        txtEmail.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtEmailKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtEmailKeyReleased(evt);
            }
        });

        tblCustomers.setAutoCreateRowSorter(true);
        tblCustomers.setBackground(new java.awt.Color(53, 53, 53));
        tblCustomers.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        tblCustomers.setForeground(java.awt.Color.white);
        tblCustomers.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tblCustomers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblCustomersMouseClicked(evt);
            }
        });
        tblCustomers.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tblCustomersKeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(tblCustomers);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(txtContact1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtContact2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtEmail))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(txtCustomerID, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtAddress, javax.swing.GroupLayout.DEFAULT_SIZE, 525, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(txtName)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(txtCustomerID)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtAddress)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtContact1)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtContact2)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtEmail))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel3.setBackground(new java.awt.Color(1, 23, 82));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Billing Details", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 12), java.awt.Color.white)); // NOI18N

        jLabel7.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        jLabel7.setForeground(java.awt.Color.white);
        jLabel7.setText("Bill No.");

        txtBillNo.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        txtBillNo.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtBillNo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtBillNoFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtBillNoFocusLost(evt);
            }
        });
        txtBillNo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtBillNoKeyReleased(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        jLabel8.setForeground(java.awt.Color.white);
        jLabel8.setText("Barcode");

        txtBarcode.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        txtBarcode.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtBarcode.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtBarcodeFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtBarcodeFocusLost(evt);
            }
        });
        txtBarcode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBarcodeActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        jLabel9.setForeground(java.awt.Color.white);
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("Item");

        txtItem.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        txtItem.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtItem.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtItemFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtItemFocusLost(evt);
            }
        });
        txtItem.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtItemKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtItemKeyReleased(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        jLabel11.setForeground(java.awt.Color.white);
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("Qty");

        txtQty.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        txtQty.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtQty.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtQtyFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtQtyFocusLost(evt);
            }
        });
        txtQty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtQtyActionPerformed(evt);
            }
        });
        txtQty.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtQtyKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtQtyKeyReleased(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        jLabel12.setForeground(java.awt.Color.white);
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("Rate");

        txtRate.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        txtRate.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtRate.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtRateFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtRateFocusLost(evt);
            }
        });
        txtRate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtRateKeyReleased(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        jLabel13.setForeground(java.awt.Color.white);
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("Total");

        txtTotal.setEditable(false);
        txtTotal.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        txtTotal.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtTotal.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtTotalFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtTotalFocusLost(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        jLabel14.setForeground(java.awt.Color.white);
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setText("Disc. %");

        txtDiscount.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        txtDiscount.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtDiscount.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtDiscountFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtDiscountFocusLost(evt);
            }
        });
        txtDiscount.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtDiscountKeyReleased(evt);
            }
        });

        jLabel15.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        jLabel15.setForeground(java.awt.Color.white);
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setText("Tax.Value");

        txtTaxValue.setEditable(false);
        txtTaxValue.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        txtTaxValue.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtTaxValue.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtTaxValueFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtTaxValueFocusLost(evt);
            }
        });

        jLabel16.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        jLabel16.setForeground(java.awt.Color.white);
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel16.setText("Gross Amt");

        txtAmount.setEditable(false);
        txtAmount.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        txtAmount.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtAmount.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtAmountFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtAmountFocusLost(evt);
            }
        });

        tblProducts.setAutoCreateRowSorter(true);
        tblProducts.setBackground(new java.awt.Color(53, 53, 53));
        tblProducts.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        tblProducts.setForeground(java.awt.Color.white);
        tblProducts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tblProducts.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblProductsMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tblProducts);

        btnAdd.setBackground(java.awt.Color.green);
        btnAdd.setFont(new java.awt.Font("Ubuntu", 1, 14)); // NOI18N
        btnAdd.setText("Add");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnUpdate.setBackground(java.awt.Color.orange);
        btnUpdate.setFont(new java.awt.Font("Ubuntu", 1, 14)); // NOI18N
        btnUpdate.setText("Update");
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        btnDelete.setBackground(java.awt.Color.red);
        btnDelete.setFont(new java.awt.Font("Ubuntu", 1, 14)); // NOI18N
        btnDelete.setText("Delete");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        btnReset.setBackground(java.awt.Color.lightGray);
        btnReset.setFont(new java.awt.Font("Ubuntu", 1, 14)); // NOI18N
        btnReset.setText("Reset");
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        jLabel10.setForeground(java.awt.Color.white);
        jLabel10.setText("Payment Type");

        cmbPaymentType.setBackground(java.awt.Color.white);
        cmbPaymentType.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        cmbPaymentType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Cash", "Credit", "Cheque" }));
        cmbPaymentType.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                cmbPaymentTypeFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                cmbPaymentTypeFocusLost(evt);
            }
        });

        jLabel17.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        jLabel17.setForeground(java.awt.Color.white);
        jLabel17.setText("Description");

        txtDescription.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        txtDescription.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtDescriptionFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtDescriptionFocusLost(evt);
            }
        });

        txtGrandTotal.setEditable(false);
        txtGrandTotal.setBackground(java.awt.Color.blue);
        txtGrandTotal.setFont(new java.awt.Font("Ubuntu", 1, 14)); // NOI18N
        txtGrandTotal.setForeground(java.awt.Color.white);
        txtGrandTotal.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        txtPaid.setBackground(java.awt.Color.blue);
        txtPaid.setFont(new java.awt.Font("Ubuntu", 1, 14)); // NOI18N
        txtPaid.setForeground(java.awt.Color.white);
        txtPaid.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtPaid.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtPaidKeyReleased(evt);
            }
        });

        jLabel18.setFont(new java.awt.Font("Ubuntu", 1, 13)); // NOI18N
        jLabel18.setForeground(java.awt.Color.white);
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setText("GRAND TOTAL");

        jLabel19.setFont(new java.awt.Font("Ubuntu", 1, 13)); // NOI18N
        jLabel19.setForeground(java.awt.Color.white);
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setText("PAID");

        jLabel20.setFont(new java.awt.Font("Ubuntu", 1, 13)); // NOI18N
        jLabel20.setForeground(java.awt.Color.white);
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel20.setText("BALANCE");

        txtBalance.setEditable(false);
        txtBalance.setBackground(java.awt.Color.red);
        txtBalance.setFont(new java.awt.Font("Ubuntu", 1, 14)); // NOI18N
        txtBalance.setForeground(java.awt.Color.white);
        txtBalance.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        btnSaveTransaction.setBackground(java.awt.Color.green);
        btnSaveTransaction.setFont(new java.awt.Font("Ubuntu", 1, 14)); // NOI18N
        btnSaveTransaction.setText("SAVE");
        btnSaveTransaction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveTransactionActionPerformed(evt);
            }
        });

        btnUpdateTransaction.setBackground(java.awt.Color.orange);
        btnUpdateTransaction.setFont(new java.awt.Font("Ubuntu", 1, 14)); // NOI18N
        btnUpdateTransaction.setText("UPDATE");
        btnUpdateTransaction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateTransactionActionPerformed(evt);
            }
        });

        btnPrintInvoice.setFont(new java.awt.Font("Ubuntu", 1, 14)); // NOI18N
        btnPrintInvoice.setText("Print");
        btnPrintInvoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrintInvoiceActionPerformed(evt);
            }
        });

        txtUnit.setEditable(false);
        txtUnit.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        txtUnit.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtUnitFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtUnitFocusLost(evt);
            }
        });

        jLabel21.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        jLabel21.setForeground(java.awt.Color.white);
        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("Unit");

        jLabel23.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        jLabel23.setForeground(java.awt.Color.white);
        jLabel23.setText("Previous Balance");

        txtPrevBalance.setEditable(false);
        txtPrevBalance.setBackground(java.awt.Color.white);
        txtPrevBalance.setFont(new java.awt.Font("Ubuntu", 1, 14)); // NOI18N
        txtPrevBalance.setForeground(java.awt.Color.red);
        txtPrevBalance.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtPrevBalance.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtPrevBalanceFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtPrevBalanceFocusLost(evt);
            }
        });

        lblCurrentDate.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        lblCurrentDate.setForeground(java.awt.Color.white);

        jLabel22.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jLabel22.setForeground(java.awt.Color.white);
        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel22.setText("CGST%");

        jLabel24.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jLabel24.setForeground(java.awt.Color.white);
        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel24.setText("Amt");

        jLabel25.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jLabel25.setForeground(java.awt.Color.white);
        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel25.setText("SGST %");

        txtSGSTPer.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        txtSGSTPer.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtSGSTPer.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtSGSTPerFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtSGSTPerFocusLost(evt);
            }
        });
        txtSGSTPer.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtSGSTPerKeyReleased(evt);
            }
        });

        txtCGSTAmt.setEditable(false);
        txtCGSTAmt.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        txtCGSTAmt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtCGSTAmt.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtCGSTAmtFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtCGSTAmtFocusLost(evt);
            }
        });

        txtCGSTPer.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        txtCGSTPer.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtCGSTPer.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtCGSTPerFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtCGSTPerFocusLost(evt);
            }
        });
        txtCGSTPer.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtCGSTPerKeyReleased(evt);
            }
        });

        jLabel26.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jLabel26.setForeground(java.awt.Color.white);
        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel26.setText("Amt");

        txtSGSTAmt.setEditable(false);
        txtSGSTAmt.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        txtSGSTAmt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtSGSTAmt.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtSGSTAmtFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtSGSTAmtFocusLost(evt);
            }
        });

        jLabel27.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jLabel27.setForeground(java.awt.Color.white);
        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel27.setText("IGST %");

        jLabel28.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jLabel28.setForeground(java.awt.Color.white);
        jLabel28.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel28.setText("Amt");

        txtIGSTAmt.setEditable(false);
        txtIGSTAmt.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        txtIGSTAmt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtIGSTAmt.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtIGSTAmtFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtIGSTAmtFocusLost(evt);
            }
        });

        txtIGSTPer.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        txtIGSTPer.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtIGSTPer.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtIGSTPerFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtIGSTPerFocusLost(evt);
            }
        });
        txtIGSTPer.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtIGSTPerKeyReleased(evt);
            }
        });

        tblBilllingList.setAutoCreateRowSorter(true);
        tblBilllingList.setBackground(new java.awt.Color(53, 53, 53));
        tblBilllingList.setFont(new java.awt.Font("Ubuntu", 0, 13)); // NOI18N
        tblBilllingList.setForeground(java.awt.Color.white);
        tblBilllingList.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tblBilllingList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblBilllingListMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tblBilllingList);

        lblTime.setForeground(java.awt.Color.white);
        lblTime.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnReset)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAdd)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnUpdate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 552, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbPaymentType, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtPrevBalance, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtGrandTotal, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                            .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtPaid, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                            .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtBalance)
                            .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 70, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(btnSaveTransaction, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnUpdateTransaction, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE))
                            .addComponent(btnPrintInvoice, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jScrollPane3)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                            .addComponent(txtItem))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtQty, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtUnit, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtRate)
                            .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtTotal)
                            .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtTaxValue)
                            .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtCGSTPer, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtCGSTAmt)
                            .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtSGSTPer)
                            .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtSGSTAmt)
                            .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtIGSTPer)
                            .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtIGSTAmt)
                            .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtAmount)
                            .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtBillNo, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblCurrentDate, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblTime, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnAdd, btnDelete, btnReset, btnUpdate});

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblCurrentDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtBillNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtItem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12)
                            .addComponent(jLabel13)
                            .addComponent(jLabel14))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtTaxValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtQty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(jLabel25)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(txtSGSTPer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel22)
                                .addComponent(jLabel24))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtCGSTPer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtCGSTAmt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(jLabel26)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(txtSGSTAmt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(jLabel27)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(txtIGSTPer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(jLabel28)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(txtIGSTAmt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdd)
                    .addComponent(btnUpdate)
                    .addComponent(btnDelete)
                    .addComponent(btnReset))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtBalance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnPrintInvoice))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(cmbPaymentType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel18)
                            .addComponent(jLabel19)
                            .addComponent(jLabel20)
                            .addComponent(btnUpdateTransaction, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnSaveTransaction, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel23)
                            .addComponent(txtPrevBalance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17)
                            .addComponent(txtGrandTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtPaid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(9, 9, 9))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jMenu1.setText("Customer");

        mnuManageCustomer.setText("Manage Customers");
        mnuManageCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuManageCustomerActionPerformed(evt);
            }
        });
        jMenu1.add(mnuManageCustomer);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        setSize(new java.awt.Dimension(1277, 790));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        // TODO add your handling code here:
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        getBillID();
        getTrans();
        getPrevTrans();
        CurrentDate();
    }//GEN-LAST:event_formWindowOpened

    private void txtIGSTPerKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtIGSTPerKeyReleased
        // TODO add your handling code here:
        try{
            if(!"".equals(txtIGSTPer.getText())){
                calcFieldVal();
            }
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, e,"IGST Percent Field Exception",JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_txtIGSTPerKeyReleased

    private void txtIGSTPerFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtIGSTPerFocusLost
        // TODO add your handling code here:
        txtIGSTPer.setBackground(Color.WHITE);
    }//GEN-LAST:event_txtIGSTPerFocusLost

    private void txtIGSTPerFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtIGSTPerFocusGained
        // TODO add your handling code here:
        txtIGSTPer.setBackground(Color.yellow);
    }//GEN-LAST:event_txtIGSTPerFocusGained

    private void txtIGSTAmtFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtIGSTAmtFocusLost
        // TODO add your handling code here:
        txtIGSTAmt.setBackground(Color.WHITE);
    }//GEN-LAST:event_txtIGSTAmtFocusLost

    private void txtIGSTAmtFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtIGSTAmtFocusGained
        // TODO add your handling code here:
        txtIGSTAmt.setBackground(Color.yellow);
    }//GEN-LAST:event_txtIGSTAmtFocusGained

    private void txtSGSTAmtFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtSGSTAmtFocusLost
        // TODO add your handling code here:
        txtSGSTAmt.setBackground(Color.WHITE);
    }//GEN-LAST:event_txtSGSTAmtFocusLost

    private void txtSGSTAmtFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtSGSTAmtFocusGained
        // TODO add your handling code here:
        txtSGSTAmt.setBackground(Color.yellow);
    }//GEN-LAST:event_txtSGSTAmtFocusGained

    private void txtCGSTPerKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCGSTPerKeyReleased
        // TODO add your handling code here:
        try{
            if(!"".equals(txtCGSTPer.getText())){
                calcFieldVal();
            }
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, e,"CGST Percent Field Exception",JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_txtCGSTPerKeyReleased

    private void txtCGSTPerFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtCGSTPerFocusLost
        // TODO add your handling code here:
        txtCGSTPer.setBackground(Color.WHITE);
    }//GEN-LAST:event_txtCGSTPerFocusLost

    private void txtCGSTPerFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtCGSTPerFocusGained
        // TODO add your handling code here:
        txtCGSTPer.setBackground(Color.yellow);
    }//GEN-LAST:event_txtCGSTPerFocusGained

    private void txtCGSTAmtFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtCGSTAmtFocusLost
        // TODO add your handling code here:
        txtCGSTAmt.setBackground(Color.WHITE);
    }//GEN-LAST:event_txtCGSTAmtFocusLost

    private void txtCGSTAmtFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtCGSTAmtFocusGained
        // TODO add your handling code here:
        txtCGSTAmt.setBackground(Color.yellow);
    }//GEN-LAST:event_txtCGSTAmtFocusGained

    private void txtSGSTPerKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSGSTPerKeyReleased
        // TODO add your handling code here:
        try{
            if(!"".equals(txtSGSTPer.getText())){
                calcFieldVal();
            }
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, e,"SGST Percent Field Exception",JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_txtSGSTPerKeyReleased

    private void txtSGSTPerFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtSGSTPerFocusLost
        // TODO add your handling code here:
        txtSGSTPer.setBackground(Color.white);
    }//GEN-LAST:event_txtSGSTPerFocusLost

    private void txtSGSTPerFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtSGSTPerFocusGained
        // TODO add your handling code here:
        txtSGSTPer.setBackground(Color.yellow);
    }//GEN-LAST:event_txtSGSTPerFocusGained

    private void txtPrevBalanceFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtPrevBalanceFocusLost
        // TODO add your handling code here:
        txtPrevBalance.setBackground(Color.WHITE);
    }//GEN-LAST:event_txtPrevBalanceFocusLost

    private void txtPrevBalanceFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtPrevBalanceFocusGained
        // TODO add your handling code here:
        txtPrevBalance.setBackground(Color.YELLOW);
    }//GEN-LAST:event_txtPrevBalanceFocusGained

    private void txtUnitFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtUnitFocusLost
        // TODO add your handling code here:
        txtUnit.setBackground(Color.WHITE);
    }//GEN-LAST:event_txtUnitFocusLost

    private void txtUnitFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtUnitFocusGained
        // TODO add your handling code here:
        txtUnit.setBackground(Color.yellow);
    }//GEN-LAST:event_txtUnitFocusGained

    private void btnPrintInvoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintInvoiceActionPerformed
        // TODO add your handling code here:
        printOptions();
    }//GEN-LAST:event_btnPrintInvoiceActionPerformed

    private void btnUpdateTransactionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateTransactionActionPerformed
        // TODO add your handling code here:
        updtTrans();
    }//GEN-LAST:event_btnUpdateTransactionActionPerformed

    private void btnSaveTransactionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveTransactionActionPerformed
        // TODO add your handling code here:
        saveTrans();
        getTrans();
    }//GEN-LAST:event_btnSaveTransactionActionPerformed

    private void txtPaidKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPaidKeyReleased
        // TODO add your handling code here:
        if(!"".equals(txtPaid.getText())){
            calcOnPaid();
        }
    }//GEN-LAST:event_txtPaidKeyReleased

    private void txtDescriptionFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtDescriptionFocusLost
        // TODO add your handling code here:
        txtDescription.setBackground(Color.WHITE);
    }//GEN-LAST:event_txtDescriptionFocusLost

    private void txtDescriptionFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtDescriptionFocusGained
        // TODO add your handling code here:
        txtDescription.setBackground(Color.yellow);
    }//GEN-LAST:event_txtDescriptionFocusGained

    private void cmbPaymentTypeFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_cmbPaymentTypeFocusLost
        // TODO add your handling code here:
        cmbPaymentType.setBackground(Color.WHITE);
    }//GEN-LAST:event_cmbPaymentTypeFocusLost

    private void cmbPaymentTypeFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_cmbPaymentTypeFocusGained
        // TODO add your handling code here:
        cmbPaymentType.setBackground(Color.yellow);
    }//GEN-LAST:event_cmbPaymentTypeFocusGained

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
        // TODO add your handling code here:
        resetFields();
    }//GEN-LAST:event_btnResetActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        // TODO add your handling code here:
        delBillData();
        finalCalcAdd();
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        // TODO add your handling code here:
        updtBillList();
        finalCalcAdd();
        resetFields();
        getBillData();
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // TODO add your handling code here:        
        if(Double.parseDouble(txtQty.getText())<=maxqty && !"".equals(txtQty.getText()) && Double.parseDouble(txtQty.getText())>0){
                calcFieldVal();
                addToBill();                
                getBillData();
                finalCalcAdd();
                resetFields();
            }
            else{
                JOptionPane.showMessageDialog(null, "Sales Quantity is more than Stock Quantity","OUT OF STOCK",JOptionPane.ERROR_MESSAGE);
                txtQty.setText("0");                
                resetFields();
            }
    }//GEN-LAST:event_btnAddActionPerformed

    private void txtAmountFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtAmountFocusLost
        // TODO add your handling code here:
        txtAmount.setBackground(Color.WHITE);
    }//GEN-LAST:event_txtAmountFocusLost

    private void txtAmountFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtAmountFocusGained
        // TODO add your handling code here:
        txtAmount.setBackground(Color.yellow);
    }//GEN-LAST:event_txtAmountFocusGained

    private void txtTaxValueFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtTaxValueFocusLost
        // TODO add your handling code here:
        txtTaxValue.setBackground(Color.WHITE);
    }//GEN-LAST:event_txtTaxValueFocusLost

    private void txtTaxValueFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtTaxValueFocusGained
        // TODO add your handling code here:
        txtTaxValue.setBackground(Color.yellow);
    }//GEN-LAST:event_txtTaxValueFocusGained

    private void txtDiscountKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDiscountKeyReleased
        // TODO add your handling code here:
        try{
            if(!"".equals(txtDiscount.getText())){
                calcFieldVal();
            }
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, e,"Discount Percent Field Exception",JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_txtDiscountKeyReleased

    private void txtDiscountFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtDiscountFocusLost
        // TODO add your handling code here:
        txtDiscount.setBackground(Color.WHITE);
    }//GEN-LAST:event_txtDiscountFocusLost

    private void txtDiscountFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtDiscountFocusGained
        // TODO add your handling code here:
        txtDiscount.setBackground(Color.yellow);
    }//GEN-LAST:event_txtDiscountFocusGained

    private void txtTotalFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtTotalFocusLost
        // TODO add your handling code here:
        txtTotal.setBackground(Color.WHITE);
    }//GEN-LAST:event_txtTotalFocusLost

    private void txtTotalFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtTotalFocusGained
        // TODO add your handling code here:
        txtTotal.setBackground(Color.yellow);
    }//GEN-LAST:event_txtTotalFocusGained

    private void txtRateKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtRateKeyReleased
        // TODO add your handling code here:
        try{
            if(!"".equals(txtRate.getText())){
                calcFieldVal();
            }
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, e,"Rate Field Exception",JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_txtRateKeyReleased

    private void txtRateFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtRateFocusLost
        // TODO add your handling code here:
        txtRate.setBackground(Color.WHITE);
    }//GEN-LAST:event_txtRateFocusLost

    private void txtRateFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtRateFocusGained
        // TODO add your handling code here:
        txtRate.setBackground(Color.yellow);
    }//GEN-LAST:event_txtRateFocusGained

    private void txtQtyKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtQtyKeyReleased
        // TODO add your handling code here:
        try{
            if(!"".equals(txtQty.getText())){
                calcFieldVal();
            }
        }catch(NumberFormatException e){
            JOptionPane.showMessageDialog(null, e,"Qty Field Exception",JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_txtQtyKeyReleased

    private void txtQtyKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtQtyKeyPressed
        // TODO add your handling code here:
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            if(Double.parseDouble(txtQty.getText())<=maxqty && !"".equals(txtQty.getText()) && Double.parseDouble(txtQty.getText())>0){
                calcFieldVal();
                addToBill();                
                getBillData();
                finalCalcAdd();
                calcOnPaid();
                resetFields();
            }
            else{
                JOptionPane.showMessageDialog(null, "Sales Quantity is more than Stock Quantity","OUT OF STOCK",JOptionPane.ERROR_MESSAGE);
                txtQty.setText("0");                
                resetFields();
            }
        }
    }//GEN-LAST:event_txtQtyKeyPressed

    private void txtQtyFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtQtyFocusLost
        // TODO add your handling code here:
        txtQty.setBackground(Color.WHITE);
    }//GEN-LAST:event_txtQtyFocusLost

    private void txtQtyFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtQtyFocusGained
        // TODO add your handling code here:
        txtQty.setBackground(Color.yellow);
    }//GEN-LAST:event_txtQtyFocusGained

    private void txtItemKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtItemKeyReleased
        // TODO add your handling code here:
        findProduct();
    }//GEN-LAST:event_txtItemKeyReleased

    private void txtItemKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtItemKeyPressed
        // TODO add your handling code here:
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            loadSelect();
            calcFieldVal();
            txtQty.requestFocus();
            txtQty.setText("");
        }
    }//GEN-LAST:event_txtItemKeyPressed

    private void txtItemFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtItemFocusLost
        // TODO add your handling code here:
        txtItem.setBackground(Color.WHITE);
    }//GEN-LAST:event_txtItemFocusLost

    private void txtItemFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtItemFocusGained
        // TODO add your handling code here:
        txtItem.setBackground(Color.yellow);
    }//GEN-LAST:event_txtItemFocusGained

    private void txtBarcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBarcodeActionPerformed
        // TODO add your handling code here:
        barcodeScan();
        resetFields();
    }//GEN-LAST:event_txtBarcodeActionPerformed

    private void txtBarcodeFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtBarcodeFocusLost
        // TODO add your handling code here:
        txtBarcode.setBackground(Color.WHITE);
    }//GEN-LAST:event_txtBarcodeFocusLost

    private void txtBarcodeFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtBarcodeFocusGained
        // TODO add your handling code here:
        txtBarcode.setBackground(Color.yellow);
    }//GEN-LAST:event_txtBarcodeFocusGained

    private void txtBillNoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBillNoKeyReleased
        // TODO add your handling code here:
        readSalesOnBillNoEntry();
        finalCalcAdd();
        readTrans();
    }//GEN-LAST:event_txtBillNoKeyReleased

    private void txtBillNoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtBillNoFocusLost
        // TODO add your handling code here:
        txtBillNo.setBackground(Color.WHITE);
    }//GEN-LAST:event_txtBillNoFocusLost

    private void txtBillNoFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtBillNoFocusGained
        // TODO add your handling code here:
        txtBillNo.setBackground(Color.yellow);
        readSalesOnBillNoEntry();
        finalCalcAdd();
        readTrans();
    }//GEN-LAST:event_txtBillNoFocusGained

    private void tblCustomersKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tblCustomersKeyReleased
        // TODO add your handling code here:
        if(evt.getKeyCode()==KeyEvent.VK_DOWN||evt.getKeyCode()==KeyEvent.VK_UP){
            loadSelectCustTbl();
        }
    }//GEN-LAST:event_tblCustomersKeyReleased

    private void tblCustomersMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblCustomersMouseClicked
        // TODO add your handling code here:
        loadSelectCustTbl();
    }//GEN-LAST:event_tblCustomersMouseClicked

    private void txtEmailKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtEmailKeyReleased
        // TODO add your handling code here:
        getCustDataToTblOnEmail();
    }//GEN-LAST:event_txtEmailKeyReleased

    private void txtEmailKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtEmailKeyPressed
        // TODO add your handling code here:
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            loadSelectCust();
        }
    }//GEN-LAST:event_txtEmailKeyPressed

    private void txtEmailFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtEmailFocusLost
        // TODO add your handling code here:
        txtEmail.setBackground(Color.WHITE);
    }//GEN-LAST:event_txtEmailFocusLost

    private void txtEmailFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtEmailFocusGained
        // TODO add your handling code here:
        txtEmail.setBackground(Color.yellow);
    }//GEN-LAST:event_txtEmailFocusGained

    private void txtContact2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtContact2KeyReleased
        // TODO add your handling code here:
        getCustDataToTblOnCon2();
    }//GEN-LAST:event_txtContact2KeyReleased

    private void txtContact2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtContact2KeyPressed
        // TODO add your handling code here:
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            loadSelectCust();
        }
    }//GEN-LAST:event_txtContact2KeyPressed

    private void txtContact2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtContact2FocusLost
        // TODO add your handling code here:
        txtContact2.setBackground(Color.WHITE);
    }//GEN-LAST:event_txtContact2FocusLost

    private void txtContact2FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtContact2FocusGained
        // TODO add your handling code here:
        txtContact2.setBackground(Color.yellow);
    }//GEN-LAST:event_txtContact2FocusGained

    private void txtContact1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtContact1KeyReleased
        // TODO add your handling code here:
        getCustDataToTblOnCon1();
    }//GEN-LAST:event_txtContact1KeyReleased

    private void txtContact1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtContact1KeyPressed
        // TODO add your handling code here:
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            loadSelectCust();
        }
    }//GEN-LAST:event_txtContact1KeyPressed

    private void txtContact1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtContact1FocusLost
        // TODO add your handling code here:
        txtContact1.setBackground(Color.WHITE);
    }//GEN-LAST:event_txtContact1FocusLost

    private void txtContact1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtContact1FocusGained
        // TODO add your handling code here:
        txtContact1.setBackground(Color.yellow);
    }//GEN-LAST:event_txtContact1FocusGained

    private void txtAddressKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtAddressKeyReleased
        // TODO add your handling code here:
        getCustDataToTblOnAdrs();
    }//GEN-LAST:event_txtAddressKeyReleased

    private void txtAddressKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtAddressKeyPressed
        // TODO add your handling code here:
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            loadSelectCust();
        }
    }//GEN-LAST:event_txtAddressKeyPressed

    private void txtAddressFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtAddressFocusLost
        // TODO add your handling code here:
        txtAddress.setBackground(Color.WHITE);
    }//GEN-LAST:event_txtAddressFocusLost

    private void txtAddressFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtAddressFocusGained
        // TODO add your handling code here:
        txtAddress.setBackground(Color.yellow);
    }//GEN-LAST:event_txtAddressFocusGained

    private void txtNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNameKeyReleased
        // TODO add your handling code here:
        getCustDataToTblOnName();
    }//GEN-LAST:event_txtNameKeyReleased

    private void txtNameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNameKeyPressed
        // TODO add your handling code here:
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            loadSelectCust();
            getTrans();
            getPrevTrans();
        }
    }//GEN-LAST:event_txtNameKeyPressed

    private void txtNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtNameFocusLost
        // TODO add your handling code here:
        txtName.setBackground(Color.WHITE);
    }//GEN-LAST:event_txtNameFocusLost

    private void txtNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtNameFocusGained
        // TODO add your handling code here:
        txtName.setBackground(Color.yellow);
    }//GEN-LAST:event_txtNameFocusGained

    private void txtCustomerIDFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtCustomerIDFocusLost
        // TODO add your handling code here:
        txtCustomerID.setBackground(Color.WHITE);
    }//GEN-LAST:event_txtCustomerIDFocusLost

    private void txtCustomerIDFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtCustomerIDFocusGained
        // TODO add your handling code here:
        txtCustomerID.setBackground(Color.yellow);
    }//GEN-LAST:event_txtCustomerIDFocusGained

    private void tblBilllingListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblBilllingListMouseClicked
        // TODO add your handling code here:
        getBillingDataToField();
    }//GEN-LAST:event_tblBilllingListMouseClicked

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        obj=null;
    }//GEN-LAST:event_formWindowClosing
    //Program to get Product Data on Mouse click
    public void getProductTblData(){
        try{
            row=tblProducts.getSelectedRow();
            String tblClick=tblProducts.getModel().getValueAt(row, 0).toString();
            count=tblProducts.getModel().getValueAt(row, 0).toString();            
            String sql="Select * from Products where pid='"+tblClick+"'";
            pst=conn.prepareStatement(sql);
            rs=pst.executeQuery();
            if(rs.next()){
                txtBarcode.setText(rs.getString("p_Barcode"));
                HSN=rs.getString("p_hsncode");
                txtItem.setText(rs.getString("p_itemName"));
                txtUnit.setText(rs.getString("p_unit"));                
                maxqty=rs.getDouble("p_qty");
                txtRate.setText(rs.getString("p_rate"));
                txtCGSTPer.setText(rs.getString("p_cgst"));
                txtSGSTPer.setText(rs.getString("p_sgst"));
                txtIGSTPer.setText(rs.getString("p_igst"));
                txtQty.requestFocus();
            }
            pst.close();
        }
        catch(SQLException e){
            JOptionPane.showMessageDialog(null, e);
        }        
    }
    private void tblProductsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblProductsMouseClicked
        // TODO add your handling code here:
        //getProductTblData();
    }//GEN-LAST:event_tblProductsMouseClicked

    private void txtQtyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtQtyActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtQtyActionPerformed

    private void mnuManageCustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuManageCustomerActionPerformed
        // TODO add your handling code here:
        ManageCustomer mc=new ManageCustomer();
        mc.setVisible(true);
    }//GEN-LAST:event_mnuManageCustomerActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ManageSales.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ManageSales.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ManageSales.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ManageSales.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ManageSales().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnPrintInvoice;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSaveTransaction;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JButton btnUpdateTransaction;
    private javax.swing.JComboBox<String> cmbPaymentType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblCurrentDate;
    private javax.swing.JLabel lblTime;
    private javax.swing.JMenuItem mnuManageCustomer;
    private javax.swing.JTable tblBilllingList;
    private javax.swing.JTable tblCustomers;
    private javax.swing.JTable tblProducts;
    private javax.swing.JTextField txtAddress;
    private javax.swing.JTextField txtAmount;
    private javax.swing.JTextField txtBalance;
    private javax.swing.JTextField txtBarcode;
    private javax.swing.JTextField txtBillNo;
    private javax.swing.JTextField txtCGSTAmt;
    private javax.swing.JTextField txtCGSTPer;
    private javax.swing.JTextField txtContact1;
    private javax.swing.JTextField txtContact2;
    private javax.swing.JTextField txtCustomerID;
    private javax.swing.JTextField txtDescription;
    private javax.swing.JTextField txtDiscount;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtGrandTotal;
    private javax.swing.JTextField txtIGSTAmt;
    private javax.swing.JTextField txtIGSTPer;
    private javax.swing.JTextField txtItem;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtPaid;
    private javax.swing.JTextField txtPrevBalance;
    private javax.swing.JTextField txtQty;
    private javax.swing.JTextField txtRate;
    private javax.swing.JTextField txtSGSTAmt;
    private javax.swing.JTextField txtSGSTPer;
    private javax.swing.JTextField txtTaxValue;
    private javax.swing.JTextField txtTotal;
    private javax.swing.JTextField txtUnit;
    // End of variables declaration//GEN-END:variables
}
