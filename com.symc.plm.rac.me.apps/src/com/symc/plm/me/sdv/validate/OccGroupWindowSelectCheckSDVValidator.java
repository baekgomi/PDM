/**
 *
 */
package com.symc.plm.me.sdv.validate;

import java.util.Map;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.cme.framework.treetable.CMEBOMTreeTable;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAppGroupBOPLine;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.AdapterUtil;
import com.teamcenter.rac.util.PlatformHelper;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : OccGroupWindowSelectCheckSDVValidator
 * Class Description :
 * 
 *  [SR150122-027][20150309]shcho, ���� �Ҵ� E/Item�� ���� DPV�� ���� �ڵ� ���� ���� �ذ� - Link������ MProduct�� ã�� �� �ֵ��� ����
 *
 * @date 2014. 4. 3.
 *
 */
public class OccGroupWindowSelectCheckSDVValidator implements ISDVValidator {
    private Registry registry = Registry.getRegistry(OccGroupWindowSelectCheckSDVValidator.class);

    /**
     * Description :
     *
     * @method :
     * @date : 2014. 4. 3.
     * @param :
     * @return :
     * @see org.sdv.core.common.ISDVValidator#validate(java.lang.String, java.util.Map)
     */
    @Override
    public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {
        try {
            InterfaceAIFComponent[] selectedTargets = CustomUtil.getCurrentApplicationTargets();

            TCComponentBOMLine bomLine = getMBOMLine((TCComponentBOPLine)selectedTargets[0]);

            // PERT ������ �ùٸ��� �Ǿ� �ִ��� Ȯ���Ѵ� (BOP �� ���� ���� = PERT �� ������� ������ ���� ����)
            if (bomLine instanceof TCComponentAppGroupBOPLine) {
                throw new ValidateSDVException(registry.getString("occBomLineWindow.OccGroup.MESSAGE", "OccGroup current target of M-Product Window open to view. \nPlease change the M-Product view."));
            }


        } catch (ValidateSDVException ve) {
            throw ve;
        } catch (Exception e) {
            throw new ValidateSDVException(e.getMessage(), e);
        }

    }

    /**
     *       MPP�� Ȱ��ȭ �Ǿ��ִ� view ���� BOMView �� �����Ͽ� BOMLine ������ ��ư���
     *       MPP �� �������� TAB View �߿� cc ������ TYPE ������ ���Ͽ� ������ BOP �ǰ�
     *       �����Ǿ� �ִ� BOMVIEW �����͸� �����´�
     *
     *  [SR150122-027][20150309]shcho, ���� �Ҵ� E/Item�� ���� DPV�� ���� �ڵ� ���� ���� �ذ� - Link������ MProduct�� ã�� �� �ֵ��� ����
     * 
     * @method getMBOMLine
     * @date 2014. 3. 31.
     * @param
     * @return TCComponentBOMLine
     * @exception
     * @throws
     * @see
     */
    public TCComponentBOMLine getMBOMLine(TCComponentBOPLine selectBOPLine) {
        String rootBomView = null;
        String targetProduct = null;
        TCComponentBOMLine rootBomline = null;
        try {
            TCComponentItemRevision itemRevision = selectBOPLine.window().getTopBOMLine().getItemRevision();
            String productCode = itemRevision.getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
            if(productCode != null) {
                targetProduct = "M".concat(productCode.substring(1));
            }
        } catch (TCException e) {
            e.printStackTrace();
        }
        IViewReference[] arrayOfIViewReference = PlatformHelper.getCurrentPage().getViewReferences();
        //ISelection iSelection = PlatformHelper.getCurrentPage().getSelection();
        for (IViewReference viewRerence : arrayOfIViewReference) {
            IViewPart localIViewPart = viewRerence.getView(false);
            if (localIViewPart == null){
                continue;
            }
            CMEBOMTreeTable cmeBOMTreeTable = (CMEBOMTreeTable) AdapterUtil.getAdapter(localIViewPart, CMEBOMTreeTable.class);
            if (cmeBOMTreeTable == null){
                continue;
            }

            rootBomline = cmeBOMTreeTable.getBOMRoot();
            try {
                rootBomView = rootBomline.getProperty(SDVPropertyConstant.BL_ITEM_ID);
            } catch (TCException e) {
                e.printStackTrace();
            }
            if (targetProduct != null && rootBomView != null){
                if (targetProduct.equals(rootBomView)){
                    return rootBomline;
                }
            }
        }
        return null;
    }

    /**
     * BOP �ֻ����� SHOP�� ����Ǿ� �ִ� BOMView ����(M7_Product) �� Ȯ���Ͽ� M_Product ID ������ ��ȯ �Ѵ�
     *
     * @method getMProduct
     * @date 2014. 3. 31.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    /* [SR150122-027][20150309]shcho, ���� �Ҵ� E/Item�� ���� DPV�� ���� �ڵ� ���� ���� �ذ� - Shop�� MProduct Link������ ���̻� ������.
    public String getMProduct(TCComponentItemRevision revision) throws TCException {
        String mProductType = null;
        String mProductId = null;
        TCComponent[] mProduct = revision.getRelatedComponents(SDVTypeConstant.MFG_TARGETS);
        if (mProduct.length == 1){
            mProductType = mProduct[0].getProperty(SDVPropertyConstant.ITEM_OBJECT_TYPE);
            if (mProductType.equals(SDVTypeConstant.EBOM_MPRODUCT_REV)){
                return mProductId = mProduct[0].getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
            }
        }
        return mProductId;
    }*/


}
