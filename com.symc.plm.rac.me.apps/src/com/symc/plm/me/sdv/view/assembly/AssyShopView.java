package com.symc.plm.me.sdv.view.assembly;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVLOVComboBox;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVText;
import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;

/**
 * Shopt ���� View
 * Class Name : AssyShopView
 * Class Description :
 * [SR140723-010][20140717] shcho, m7_JPH �Ӽ��� Ÿ���� �������� �ε� �Ҽ������� ����. �Ҽ�������5�ڸ����� �Է°���.
 * 
 * @date 2013. 12. 3.
 * 
 */
public class AssyShopView extends AbstractSDVViewPane {
    private SDVText txtProduct;
    private Label lblShopKorName;
    private SDVText txtShopKorName;
    private Label lboShopEngName;
    private SDVText txtShopEngName;
    private Label lblJph;
    private Composite composite;
    private SDVText txtJph;
    private Label lblAllowance;
    private SDVText txtAllowance;
    private Label lblVehicleKorName;
    private SDVText txtVehicleKorName;
    private Label lblVehicleEngName;
    private SDVText txtVehicleEngName;
    private SDVLOVComboBox lovShop;
    private TCSession tcSession = null;
    private String vehicleCode = "";

    private IDataMap curDataMap = null;
    // MProduct Top Revision
    private TCComponentItemRevision mProductItemRevision = null;
    // EBOM Product Item (M product�� �������� �ʾ��� ��� ã�� ���ؼ� �ʿ�)
    private TCComponentItem ebomProductItem = null;

    public AssyShopView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initUI()
     */
    @Override
    protected void initUI(Composite parent) {
        tcSession = SDVBOPUtilities.getTCSession();
        Registry registry = Registry.getRegistry(AssyShopView.class);
        // FillLayout fillLayout = new FillLayout(SWT.HORIZONTAL);
        // setLayout(fillLayout);
        // fillLayout.marginWidth = 5;
        // fillLayout.marginHeight = 5;

        Group grpShop = new Group(parent, SWT.NONE);
        grpShop.setText(registry.getString("ShopInform.NAME"));
        // grpShop.setLayout(new FillLayout(SWT.HORIZONTAL));
        grpShop.setLayout(new GridLayout(1, false));

        Composite compositeInform = new Composite(grpShop, SWT.NONE);
        GridLayout gl_compositeInform = new GridLayout(2, false);
        gl_compositeInform.marginLeft = 10;
        compositeInform.setLayout(gl_compositeInform);

        Label lblShop = new Label(compositeInform, SWT.NONE);
        lblShop.setText(registry.getString("ShopCode.NAME"));

        lovShop = new SDVLOVComboBox(compositeInform, SWT.BORDER, tcSession, "M7_BOPA_SHOP_CODE");
        GridData gd_txtShop = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtShop.widthHint = 200;
        lovShop.setLayoutData(gd_txtShop);

        Label lblProduct = new Label(compositeInform, SWT.NONE);
        lblProduct.setText(registry.getString("ProductCode.NAME"));

        txtProduct = new SDVText(compositeInform, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtProduct = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtProduct.widthHint = 100;
        txtProduct.setLayoutData(gd_txtProduct);

        lblShopKorName = new Label(compositeInform, SWT.NONE);
        lblShopKorName.setText(registry.getString("ShopKorName.NAME"));

        txtShopKorName = new SDVText(compositeInform, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtShopKorName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtShopKorName.widthHint = 340;
        txtShopKorName.setLayoutData(gd_txtShopKorName);

        lboShopEngName = new Label(compositeInform, SWT.NONE);
        lboShopEngName.setText(registry.getString("ShopEngName.NAME"));

        txtShopEngName = new SDVText(compositeInform, SWT.BORDER | SWT.SINGLE);
        GridData gd_text = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_text.widthHint = 340;
        txtShopEngName.setLayoutData(gd_text);

        lblJph = new Label(compositeInform, SWT.NONE);
        lblJph.setText(registry.getString("JPH.NAME"));

        composite = new Composite(compositeInform, SWT.NONE);
        GridLayout gl_composite = new GridLayout(6, false);
        gl_composite.marginWidth = 0;
        composite.setLayout(gl_composite);

        txtJph = new SDVText(composite, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtJph = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
        gd_txtJph.widthHint = 100;
        txtJph.setLayoutData(gd_txtJph);
        new Label(composite, SWT.NONE);
        // [SR140723-010][20140717] shcho, m7_JPH �Ӽ��� Ÿ���� �������� �ε� �Ҽ������� ����. �Ҽ�������5�ڸ����� �Է°���.
        // txtJph.setInputType(SDVText.NUMERIC);
        txtJph.setInputType(SDVText.DOUBLE);
        txtJph.setTextLimit(5);

        lblAllowance = new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        lblAllowance.setText(registry.getString("Allowance.NAME"));

        txtAllowance = new SDVText(composite, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtAllowance = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtAllowance.widthHint = 100;
        txtAllowance.setLayoutData(gd_txtAllowance);
        txtAllowance.setInputType(SDVText.DOUBLE);

        lblVehicleKorName = new Label(compositeInform, SWT.NONE);
        lblVehicleKorName.setText(registry.getString("VehicleKorName.NAME"));

        txtVehicleKorName = new SDVText(compositeInform, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtVehicleKorName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtVehicleKorName.widthHint = 340;
        txtVehicleKorName.setLayoutData(gd_txtVehicleKorName);

        lblVehicleEngName = new Label(compositeInform, SWT.NONE);
        lblVehicleEngName.setText(registry.getString("VehicleEngName.NAME"));

        txtVehicleEngName = new SDVText(compositeInform, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtVehicleEngName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtVehicleEngName.widthHint = 340;
        txtVehicleEngName.setLayoutData(gd_txtVehicleEngName);

        lovShop.setMandatory(true);
        txtShopKorName.setMandatory(true);
        txtShopEngName.setMandatory(true);
        txtProduct.setMandatory(true);
        txtJph.setMandatory(true);
        txtAllowance.setMandatory(true);

    }

    /**
     * Intit Data Load
     * 
     * @method loadInitData
     * @date 2013. 11. 19.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void loadInitData(IDataMap dataMap) {

        if (dataMap == null)
            return;

        String productCode = "";
        if (dataMap.containsKey(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE)) {
            productCode = (String) dataMap.getStringValue(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE); // Product Code
            txtProduct.setText(productCode);
            txtProduct.setEditable(false);
        }

        if (dataMap.containsKey(SDVPropertyConstant.SHOP_REV_VEHICLE_CODE)) {
            vehicleCode = (String) dataMap.getStringValue(SDVPropertyConstant.SHOP_REV_VEHICLE_CODE); // vehicle Code
        }
        // M Product
        Object mproductObj = dataMap.getValue(SDVTypeConstant.BOP_MPRODUCT_REVISION);
        if (mproductObj != null)
            mProductItemRevision = (TCComponentItemRevision) mproductObj;

        Object ebomProductObj = dataMap.getValue(SDVTypeConstant.EBOM_PRODUCT_ITEM);
        if (ebomProductObj != null)
            ebomProductItem = (TCComponentItem) ebomProductObj;

    }

    /**
     * �ۼ��� ����� Data
     * 
     * @method saveData
     * @date 2013. 11. 20.
     * @param
     * @return Map<String,Object>
     * @exception
     * @throws
     * @see
     */
    private IDataMap saveData() {

        RawDataMap savedDataMap = new RawDataMap();

        savedDataMap.put(SDVPropertyConstant.SHOP_REV_SHOP_CODE, lovShop.getSelectedString());
        savedDataMap.put(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE, txtProduct.getText());
        savedDataMap.put(SDVPropertyConstant.ITEM_OBJECT_NAME, txtShopKorName.getText());
        savedDataMap.put(SDVPropertyConstant.SHOP_ENG_NAME, txtShopEngName.getText());
        savedDataMap.put(SDVPropertyConstant.SHOP_REV_JPH, txtJph.getText());
        savedDataMap.put(SDVPropertyConstant.SHOP_REV_ALLOWANCE, txtAllowance.getText());
        savedDataMap.put(SDVPropertyConstant.SHOP_VEHICLE_KOR_NAME, txtVehicleKorName.getText());
        savedDataMap.put(SDVPropertyConstant.SHOP_VEHICLE_ENG_NAME, txtVehicleEngName.getText());

        if (mProductItemRevision != null) {
            savedDataMap.put(SDVTypeConstant.BOP_MPRODUCT_REVISION, mProductItemRevision, IData.OBJECT_FIELD);
            savedDataMap.put(SDVTypeConstant.EBOM_PRODUCT_ITEM, ebomProductItem, IData.OBJECT_FIELD);
        } else {
            try {
                // MProduct�� �������� �ʰ� �Էµ� ��
                if (!txtProduct.getText().isEmpty()) {
                    TCComponentItem ebomProductItem = SDVBOPUtilities.FindItem(txtProduct.getText(), SDVTypeConstant.EBOM_PRODUCT_ITEM); // EBOM Product Item
                    if (ebomProductItem != null) {

                        String proJectNo = ebomProductItem.getLatestItemRevision().getProperty("s7_PROJECT_CODE"); // Project Item
                        TCComponentItem projectItem = SDVBOPUtilities.FindItem(proJectNo, SDVTypeConstant.EBOM_PROJECT_ITEM);
                        // Project �˻� - Vehicle No ������
                        vehicleCode = projectItem.getLatestItemRevision().getProperty("s7_VEHICLE_NO");
                        String mproductCode = "M".concat(ebomProductItem.getProperty(SDVPropertyConstant.ITEM_ITEM_ID).substring(1));
                        // MBOM Product Item �˻�
                        TCComponentItem mProductItem = SDVBOPUtilities.FindItem(mproductCode, "Item");
                        if (mProductItem != null) {
                            mProductItemRevision = mProductItem.getLatestItemRevision();
                            this.ebomProductItem = ebomProductItem;
                            // EBOM Product ���� ����
                            savedDataMap.put(SDVTypeConstant.EBOM_PRODUCT_ITEM, ebomProductItem, IData.OBJECT_FIELD);
                            // MProduct ���� ����
                            savedDataMap.put(SDVTypeConstant.BOP_MPRODUCT_REVISION, mProductItemRevision, IData.OBJECT_FIELD);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        savedDataMap.put(SDVPropertyConstant.SHOP_REV_VEHICLE_CODE, vehicleCode);

        return savedDataMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#setLocalDataMap(org.sdv.core.common.data.IDataMap)
     */
    @Override
    public void setLocalDataMap(IDataMap dataMap) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getLocalDataMap()
     */
    @Override
    public IDataMap getLocalDataMap() {
        curDataMap = saveData();
        return curDataMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getLocalSelectDataMap()
     */
    @Override
    public IDataMap getLocalSelectDataMap() {
        curDataMap = saveData();
        return curDataMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getRootContext()
     */
    @Override
    public Composite getRootContext() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getInitOperation()
     */
    @Override
    public AbstractSDVInitOperation getInitOperation() {
        return new InitOperation();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initalizeData(int, org.sdv.core.common.IViewPane, org.sdv.core.common.data.IDataSet)
     */
    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
        if (result == SDVInitEvent.INIT_FAILED)
            return;
        if (dataset == null)
            return;
        IDataMap dataMap = dataset.getDataMap(this.getId());
        loadInitData(dataMap);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#uiLoadCompleted()
     */
    @Override
    public void uiLoadCompleted() {

    }

    /**
     * �ʱ� Data Load Operation
     * Class Name : InitOperation
     * Class Description :
     * 
     * @date 2013. 12. 3.
     * 
     */
    public class InitOperation extends AbstractSDVInitOperation {

        /*
         * (non-Javadoc)
         * 
         * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
         */
        @Override
        public void executeOperation() throws Exception {

            IDataMap displayDataMap = new RawDataMap();
            try {
                MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
                // ���� BOM WINDOW
                TCComponentBOMWindow bomWindow = mfgApp.getBOMWindow();

                if (bomWindow == null)
                    return;
                // M Product ���� ���� Ȯ��
                TCComponentItemRevision mTopItemRevision = bomWindow.getTopBOMLine().getItemRevision();

                // FIXME: M PRODUCT Ÿ������ �����ؾ���
                if (!mTopItemRevision.getType().equals(SDVTypeConstant.BOP_MPRODUCT_REVISION))
                    return;

                // EBOM Product �˻�
                String productCode = "P".concat(mTopItemRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID).substring(1));
                TCComponentItem ebomProductItem = SDVBOPUtilities.FindItem(productCode, SDVTypeConstant.EBOM_PRODUCT_ITEM);

                if (ebomProductItem == null)
                    return;
                // Project Code �� ������
                String proJectNo = ebomProductItem.getLatestItemRevision().getProperty("s7_PROJECT_CODE");
                ebomProductItem = SDVBOPUtilities.FindItem(proJectNo, SDVTypeConstant.EBOM_PROJECT_ITEM);
                if (ebomProductItem == null)
                    return;

                // Project �˻� - Vehicle No ������
                String vehichleNo = ebomProductItem.getLatestItemRevision().getProperty("s7_VEHICLE_NO");
                displayDataMap = new RawDataMap();
                // Project Code
                displayDataMap.put(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE, productCode);
                // Vehicle No
                displayDataMap.put(SDVPropertyConstant.SHOP_REV_VEHICLE_CODE, vehichleNo);
                // MProduct Revision
                displayDataMap.put(SDVTypeConstant.BOP_MPRODUCT_REVISION, mTopItemRevision, IData.OBJECT_FIELD);
                displayDataMap.put(SDVTypeConstant.EBOM_PRODUCT_ITEM, ebomProductItem, IData.OBJECT_FIELD);

                DataSet viewDataSet = new DataSet();
                viewDataSet.addDataMap(AssyShopView.this.getId(), displayDataMap);
                setData(viewDataSet);

            } catch (TCException e) {
                e.printStackTrace();
            }
        }

    }

}
