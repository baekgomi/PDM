/**
 * 
 */
package org.sdv.core.common;

import java.util.EventListener;

import org.sdv.core.ui.view.event.SDVViewStateEvent;

/**
 * Class Name : ISDVViewListener
 * Class Description : 
 * @date 2013. 10. 2.
 *
 */
public interface ISDVViewListener extends EventListener {
    
    /**
     * View�� ���°� ����� ���(Refresh�� �ʿ��� ���)
     * �ٸ� ���۷��̼ǿ� ���� �����Ͱ� �ε�� ��쿡�� ������ ���� �Ѿ�´�.
     * @method viewStateChanged 
     * @date 2013. 11. 21.
     * @param  
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void viewStateChanged(SDVViewStateEvent evt);
    
    /**
     * View�� UI �ε���  �ʱ� ������ ó�� ���۷��̼��� �Ϸ����  ȣ��Ǵ� �̺�Ʈ, 
     * �ʱ� ������ ���� Event�� �Ѿ�´�.
     * 
     * @method viewUIInitialized 
     * @date 2013. 11. 21.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void viewUIInitialized(SDVViewStateEvent evt);
    
    /**
     * View�� ���� �θ� Contents�� ��� �����Ͱ� �ε� �� �Ŀ� ȣ��Ǵ� �̺�Ʈ, 
     * �ٸ� View�� ������ �ε� �Ŀ� ó���� �۾��� �� �̺�Ʈ���� ó���Ѵ�.
     * 
     * @method viewUICompleted 
     * @date 2013. 11. 21.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void viewUICompleted(SDVViewStateEvent evt);
    
    
}
