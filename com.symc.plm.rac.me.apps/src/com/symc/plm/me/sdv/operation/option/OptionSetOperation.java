/**
 * 
 */
package com.symc.plm.me.sdv.operation.option;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.JDialog;

import com.symc.plm.me.utils.variant.VariantErrorCheck;
import com.symc.plm.me.utils.variant.VariantOption;
import com.symc.plm.me.utils.variant.VariantValue;
import com.symc.plm.me.utils.variant.WaitProgressBar;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCVariantService;
import com.teamcenter.rac.pse.variants.modularvariants.ConditionElement;
import com.teamcenter.rac.pse.variants.modularvariants.ConstraintsModel;
import com.teamcenter.rac.pse.variants.modularvariants.MVLLexer;
import com.teamcenter.rac.pse.variants.modularvariants.OptionConstraint;
import com.teamcenter.rac.util.MessageBox;

/**
 * Class Name : OptionSetOperation
 * Class Description :
 * 
 * @date 2013. 11. 12.
 * 
 */
public class OptionSetOperation extends AbstractAIFOperation {

    private ArrayList<VariantOption> selectedLineOptionSet = null;
    private TCComponentBOMLine selectedLine = null;
    private Vector<String[]> userDefineErrorList = null;
    private Vector<String[]> moduleConstraintList = null;
    private Vector<Vector<?>> allData = null;
    private WaitProgressBar waitProgress = null;

    private int operationType = -1;
    public static final int OPTION_SAVE = 100;
    public static final int USER_DEFINE_ERROR_DELETE = 200;
    public static final int MODULE_CONSTRAINT_DELETE = 300;
    private JDialog parent = null;

    public OptionSetOperation(ArrayList<VariantOption> selectedLineOptionSet, TCComponentBOMLine selectedLine, Vector<String[]> userDefineErrorList, Vector<String[]> moduleConstraintList, Vector<Vector<?>> allData, JDialog parent, WaitProgressBar waitProgress) {
        this.selectedLineOptionSet = selectedLineOptionSet;
        this.selectedLine = selectedLine;
        this.userDefineErrorList = userDefineErrorList;
        this.moduleConstraintList = moduleConstraintList;
        this.allData = allData;
        this.parent = parent;
        this.waitProgress = waitProgress;
        this.operationType = OPTION_SAVE;
    }

    @Override
    public void executeOperation() throws Exception {
        try {
            if (operationType == OPTION_SAVE) {
                optionApply();
                MessageBox.post(AIFUtility.getActiveDesktop(), "���������� ���� �Ǿ����ϴ�.", "INFORMATION", MessageBox.WARNING);

            }

            if (waitProgress != null) {
                waitProgress.dispose();
            }

            if (parent != null) {
                parent.dispose();
            }
        } catch (Exception e) {
            e.printStackTrace();
            waitProgress.setShowButton(true);
            throw e;
            // waitProgress.close("Fail", false);
        }

    }

    /**
     * Dialog���� ������ �ɼ��� ������ ����.
     * 
     * @throws TCException
     */
    private void optionApply() throws TCException {

        waitProgress.setStatus("Option updating...", true);
        apply(allData, userDefineErrorList, selectedLine);
    }

    /**
     * ���ǵ� �ɼ�, ��ȿ���� BOM line�� �ݿ��ϰ� ������.
     * 
     * @param allData
     * @param userDefineErrorList
     * @param selectedLine
     * @throws TCException
     */
    private void apply(Vector<Vector<?>> allData, Vector<String[]> userDefineErrorList, TCComponentBOMLine selectedLine) throws TCException {

        InterfaceAIFComponent[] targets = AIFUtility.getCurrentApplication().getTargetComponents();
        if (targets[0] instanceof TCComponentBOMLine) {
            try {

                // ������ ���� �Ǿ� �ִ� �ɼ��� ������.
                ArrayList<String> appliedOption = new ArrayList<String>();
                for (VariantOption option : selectedLineOptionSet) {
                    appliedOption.add(option.getOptionName());
                }

                // Registry registry = Registry.getRegistry(this);

                HashMap<String, VariantErrorCheck> notUseErrorMap = new HashMap<String, VariantErrorCheck>();
                HashMap<String, VariantErrorCheck> notDefineErrorMap = new HashMap<String, VariantErrorCheck>();

                TCVariantService tcvariantservice = selectedLine.getSession().getVariantService();

                // ������ �ɼ� �ڵ尪�� Ȯ���մϴ�.
                // waitProgress.setStatus(registry.getString("variant.loadingOptionCode"), true);
                // FIXME: registry������ ����
                waitProgress.setStatus("������ �ɼ� �ڵ尪�� Ȯ���մϴ�.", true);
                for (Vector<?> row : allData) {
                    VariantValue value = (VariantValue) row.get(0);
                    VariantOption option = value.getOption();

                    if (value.getValueStatus() == VariantValue.VALUE_USE) {

                        if (!value.isNew()) {
                            continue;
                        }
                        if (appliedOption.contains(option.getOptionName())) {
                            continue;
                        }

                        // üũ�ڽ��� ���� �� ���
                    } else if (value.getValueStatus() == VariantValue.VALUE_NOT_USE) {

                        VariantErrorCheck notUseErrorcheck = notUseErrorMap.get(option.getOptionName());
                        if (notUseErrorcheck == null) {
                            notUseErrorcheck = new VariantErrorCheck();
                            notUseErrorcheck.type = "inform";
                            notUseErrorcheck.message = VariantValue.TC_MESSAGE_NOT_USE;
                        }

                        // condition �߰�
                        ConditionElement condition = new ConditionElement();
                        if (notUseErrorcheck.getConditionSize() == 0) {
                            condition.ifOrAnd = "if";
                        } else {
                            condition.ifOrAnd = "or";
                        }
                        condition.item = MVLLexer.mvlQuoteId(selectedLine.getItem().getProperty("item_id"), true);
                        condition.op = "=";
                        condition.option = MVLLexer.mvlQuoteId(option.getOptionName(), false);
                        condition.value = value.getValueName();
                        condition.valueIsString = true;
                        condition.fullName = condition.item + ":" + condition.option;
                        notUseErrorcheck.addCondition(condition);

                        notUseErrorMap.put(option.getOptionName(), notUseErrorcheck);
                        // �ɼ� Ʈ������ �ɼ����̺�� �̵� ���� ���� ���� Value
                    } else {

                        VariantErrorCheck notDefineErrorcheck = notDefineErrorMap.get(option.getOptionName());
                        if (notDefineErrorcheck == null) {
                            notDefineErrorcheck = new VariantErrorCheck();
                            notDefineErrorcheck.type = "inform";
                            notDefineErrorcheck.message = VariantValue.TC_MESSAGE_NOT_DEFINE;
                        }

                        // condition �߰�
                        ConditionElement condition = new ConditionElement();
                        if (notDefineErrorcheck.getConditionSize() == 0) {
                            condition.ifOrAnd = "if";
                        } else {
                            condition.ifOrAnd = "or";
                        }
                        condition.item = MVLLexer.mvlQuoteId(selectedLine.getItem().getProperty("item_id"), true);

                        condition.op = "=";
                        condition.option = MVLLexer.mvlQuoteId(option.getOptionName(), false);
                        condition.value = value.getValueName();
                        condition.valueIsString = true;
                        condition.fullName = condition.item + ":" + condition.option;
                        notDefineErrorcheck.addCondition(condition);

                        notDefineErrorMap.put(option.getOptionName(), notDefineErrorcheck);
                    }
                }

                selectedLine.window().save();

                StringBuilder sb = new StringBuilder();
                Set<String> set = notUseErrorMap.keySet();
                Iterator<String> its = set.iterator();
                while (its.hasNext()) {
                    String key = its.next();
                    VariantErrorCheck notUseErrorcheck = notUseErrorMap.get(key);
                    String msg = VariantValue.TC_MESSAGE_NOT_USE;
                    ConditionElement[] elements = notUseErrorcheck.getCondition();
                    for (int i = 0; elements != null && i < elements.length; i++) {
                        if (i == 0) {
                            msg += "[";
                        }
                        msg += (i > 0 ? ", " : "") + elements[i].value;
                        if (i == elements.length - 1) {
                            msg += "]";
                        }
                    }
                    notUseErrorcheck.message = msg;
                    notUseErrorcheck.appendConstraints(sb);
                }

                set = notDefineErrorMap.keySet();
                its = set.iterator();
                while (its.hasNext()) {
                    String key = its.next();
                    VariantErrorCheck notDefineErrorcheck = notDefineErrorMap.get(key);
                    String msg = VariantValue.TC_MESSAGE_NOT_DEFINE;
                    ConditionElement[] elements = notDefineErrorcheck.getCondition();
                    for (int i = 0; elements != null && i < elements.length; i++) {
                        if (i == 0) {
                            msg += "[";
                        }
                        msg += (i > 0 ? ", " : "") + elements[i].value;
                        if (i == elements.length - 1) {
                            msg += "]";
                        }
                    }
                    notDefineErrorcheck.message = msg;
                    notDefineErrorcheck.appendConstraints(sb);
                }

                // waitProgress.setStatus(registry.getString("variant.checkUserDefineError"), true);
                // FIXME: registry������ ����
                waitProgress.setStatus("����� ���� ������ üũ�մϴ�.", true);
                // ����� ���� ���� üũ
                VariantErrorCheck userDefineErrorcheck = null;
                if (userDefineErrorList != null && !userDefineErrorList.isEmpty()) {
                    for (String[] errorInfo : userDefineErrorList) {
                        ConditionElement condition = new ConditionElement();

                        if (errorInfo[0] != null && !errorInfo[0].equals("")) {
                            if (userDefineErrorcheck != null) {
                                userDefineErrorcheck.appendConstraints(sb);
                            }
                            userDefineErrorcheck = new VariantErrorCheck();
                            userDefineErrorcheck.type = errorInfo[0];
                        }
                        if (errorInfo[1] != null && !errorInfo[1].equals("")) {
                            userDefineErrorcheck.message = errorInfo[1];

                        }
                        condition.ifOrAnd = errorInfo[2];
                        condition.item = errorInfo[3];
                        condition.op = errorInfo[5];
                        condition.option = errorInfo[4];
                        condition.value = errorInfo[6];
                        condition.valueIsString = true;
                        condition.fullName = errorInfo[3] + ":" + errorInfo[4];
                        userDefineErrorcheck.addCondition(condition);
                    }
                    userDefineErrorcheck.appendConstraints(sb);
                }

                // waitProgress.setStatus(registry.getString("variant.checkConstraint"), true);
                // FIXME: registry������ ����
                waitProgress.setStatus("���� ��� ���� ������ üũ�մϴ�.", true);
                // ���� ��� ���� ����
                OptionConstraint moduleConstraintCheck = null;
                if (moduleConstraintList != null && !moduleConstraintList.isEmpty()) {
                    Vector<ConditionElement> conditionVec = new Vector<ConditionElement>();
                    for (String[] moduleConstraint : moduleConstraintList) {
                        ConditionElement condition = new ConditionElement();
                        if (moduleConstraint[0] != null && !moduleConstraint[0].equals("")) {
                            if (moduleConstraintCheck != null) {
                                ConditionElement[] conditionElms = conditionVec.toArray(new ConditionElement[conditionVec.size()]);
                                moduleConstraintCheck.setCondition(conditionElms);
                                if (conditionElms != null && conditionElms.length > 0) {
                                    sb.append("if ");
                                    ConstraintsModel.appendCondition(conditionElms, sb);
                                    sb.append(" then\n ");
                                    moduleConstraintCheck.appendConstraint(sb);
                                    sb.append("\nendif");
                                }
                                sb.append((char) 13);
                                conditionVec.clear();
                            }
                            moduleConstraintCheck = new OptionConstraint();
                            moduleConstraintCheck.type = moduleConstraint[0];
                            moduleConstraintCheck.fullName = moduleConstraint[4] + ":" + moduleConstraint[1];
                            moduleConstraintCheck.item = moduleConstraint[4];
                            moduleConstraintCheck.option = moduleConstraint[1];
                            moduleConstraintCheck.value = moduleConstraint[2];
                            moduleConstraintCheck.valueIsString = true;
                        }
                        condition.ifOrAnd = moduleConstraint[3];
                        condition.item = moduleConstraint[4];
                        condition.op = moduleConstraint[6];
                        condition.option = moduleConstraint[5];
                        condition.value = moduleConstraint[7];
                        condition.valueIsString = true;
                        condition.fullName = moduleConstraint[4] + ":" + moduleConstraint[5];
                        conditionVec.add(condition);
                    }
                    ConditionElement[] conditionElms = conditionVec.toArray(new ConditionElement[conditionVec.size()]);
                    moduleConstraintCheck.setCondition(conditionElms);
                    if (conditionElms != null && conditionElms.length > 0) {
                        sb.append("if ");
                        ConstraintsModel.appendCondition(conditionElms, sb);
                        sb.append(" then\n ");
                        moduleConstraintCheck.appendConstraint(sb);
                        sb.append("\nendif");
                    }
                    sb.append((char) 13);
                }

                try {

                    // waitProgress.setStatus(registry.getString("variant.addValidation"), true);
                    // FIXME: registry������ ����
                    waitProgress.setStatus("��ȿ�� �˻� �� ���� ������ �߰��մϴ�.", true);
                    if (notUseErrorMap.size() > 0 || notDefineErrorMap.size() > 0 || (userDefineErrorcheck != null && userDefineErrorcheck.getConditionSize() > 0) || (moduleConstraintList != null && !moduleConstraintList.isEmpty())) {
                        tcvariantservice.setLineMvl(selectedLine, sb.toString());
                    } else {
                        tcvariantservice.setLineMvl(selectedLine, "");
                    }
                } catch (TCException tce) {

                    waitProgress.setStatus(tce.getDetailsMessage());
                    System.out.println(sb.toString());
                    tce.printStackTrace();
                    throw tce;
                } finally {
                    // �����Ǿ��� ��ȿ�� üũ���� �ٲ�����Ƿ� Window�� �����ؾ� �ɼ� ������ ������.
                    selectedLine.window().save();
                }

            } catch (TCException e) {
                e.printStackTrace();
                throw e;
            } finally {
                selectedLine.window().save();
            }
        }
    }
}
