package edu.clemson.lph.civet.addons;

import java.awt.ContainerOrderFocusTraversalPolicy;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;

import javax.swing.JComboBox;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;

@SuppressWarnings("serial")
class ManualOrderFocusTraversalPolicy extends ContainerOrderFocusTraversalPolicy {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private ArrayList<Component> aComponents = new ArrayList<Component>();

	public ManualOrderFocusTraversalPolicy() {
		// TODO Auto-generated constructor stub
	}
	
	// Add controls to list manually in tab order.
	void addControl( Component control ) {
		aComponents.add(control);
	}

	/**
	 * Returns the Component that should receive the focus after aComponent.
	 */
	@Override
	public Component getComponentAfter(Container aContainer, Component aComponent) {
		// This nonsense is because when JComboBox is typed <String> the component we actually
		// leave is a borderless text box.  The JComboBox is its parent.  That is what we have mapped.
		if( aComponent.getParent().getClass() == JComboBox.class )
			aComponent = aComponent.getParent();
		Component cNext = null;
		int iIndex = aComponents.indexOf(aComponent);
		// Note: above returns -1 if not found so increments to zero or first component.  A logical failsafe.
		iIndex++;
		iIndex = iIndex % aComponents.size();
		cNext = aComponents.get(iIndex);
		while( !cNext.isVisible() || !cNext.isEnabled() ) {
			if( cNext == aComponent ) {
				logger.info("getComponentAfter() looped around to itself");
				return null;  // This should never happen means we looped.
			}
			iIndex++;
			iIndex = iIndex % aComponents.size();
			cNext = aComponents.get(iIndex);	
			if( cNext == aComponent )
				cNext = super.getComponentAfter(aContainer, aComponent);
		}
		return cNext;
	}

	
	/** 
	 * Returns the Component that should receive the focus before aComponent.
	 */
	@Override
	public Component getComponentBefore(Container aContainer, Component aComponent) {
		Component cNext = aComponent;
		int iIndex = aComponents.indexOf(aComponent);
		// Note: above returns -1 if not found.  Decrement is not a good solution
		if( iIndex < 0 ) 
			iIndex = 0;
		else if( iIndex == 0 ) 
			iIndex = aComponents.size() - 1;
		else 
			iIndex--;
		cNext = aComponents.get(iIndex);
		while( !cNext.isVisible() || !cNext.isEnabled() ) {
			if( cNext == aComponent ) {
				logger.info("getComponentBefore() looped around to itself");
				return null;  // This should never happen means we looped.
			}
			if( iIndex < 0 ) 
				iIndex = 0;
			else if( iIndex == 0 ) 
				iIndex = aComponents.size() - 1;
			else 
				iIndex--;
			cNext = aComponents.get(iIndex);
			if( cNext == aComponent )
				cNext = super.getComponentBefore(aContainer, aComponent);
		}
		return cNext;
	}
	
	/**
	 * Returns the default Component to focus.
	 */
	@Override
	public Component getDefaultComponent(Container aContainer) {
		return getFirstComponent( aContainer );
	}
	
	/**
	 * Returns the first Component in the traversal cycle.
	 */
	@Override
	public Component getFirstComponent(Container aContainer) {
		if( aComponents.size() > 0 )
			return aComponents.get(0);
		else
			return super.getFirstComponent(aContainer);
	}

}
