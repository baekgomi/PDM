/**
 * 
 */
package org.sdv.core.ui.view.layout;

import org.eclipse.swt.widgets.Composite;

/**
 * Class Name : GridLayoutViewPane
 * Class Description : 
 * @date 2013. 10. 22.
 *
 */
public class GridLayoutViewPane extends AbstractLayoutPane {

    public GridLayoutViewPane(Composite parent, int style, String id) {
        super(parent, style, id);
    }
    /**
     * Description :
     * @method :
     * @date : 2013. 12. 3.
     * @author : CS.Park
     * @param :
     * @return : 
     * @see org.sdv.core.ui.view.layout.AbstractLayoutPane#getLayoutComposite()
     */
    @Override
    public Composite getLayoutComposite() {
        return this;
    }

    /**
     * Description :
     * @method :
     * @date : 2013. 12. 3.
     * @author : CS.Park
     * @param :
     * @return : 
     * @see org.sdv.core.ui.view.layout.AbstractLayoutPane#getLayoutType()
     */
    @Override
    protected LayoutType getLayoutType() {
        return LayoutType.GRID_LAYOUT;
    }


}
