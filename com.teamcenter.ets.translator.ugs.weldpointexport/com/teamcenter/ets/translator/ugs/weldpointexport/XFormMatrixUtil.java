package com.teamcenter.ets.translator.ugs.weldpointexport;

import javax.media.j3d.Transform3D;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.teamcenter.tstk.util.log.ITaskLogger;

/**
 * Co2 �������� ��� �������� �������� ������ �����Ƿ� �̰��� "bl_plmxml_occ_xform" ������ �Է��ϱ�����
 * �ʿ��� Matrix ������ �����ϴ� Class��
 * Co2 ������ ���̰� �ִ� �������� �ϰ� �����Ƿ� ������ ������ ���̸� ��ȯ�ϴ� ������ �����ϰ� �ִ�.
 * [NON-SR][20160503] Taeku.Jeong
 * @author Taeku
 *
 */
public class XFormMatrixUtil {
	
	private double calculatedLength = 0.d;
	private boolean xAxisReflect = false;
	private boolean yAxisReflect = false;
	private boolean zAxisReflect = false;
	
	ITaskLogger m_zTaskLogger;
	StringBuffer buffer;
	boolean isDebug = false;
	
	public XFormMatrixUtil(){
		
	}
	
	public XFormMatrixUtil(ITaskLogger m_zTaskLogger, StringBuffer buffer, boolean isDebug){
		this.m_zTaskLogger = m_zTaskLogger;
		this.buffer = buffer;
		this.isDebug = isDebug;
	}
	
	private void addLog(String msg){
		if( isDebug ){
			m_zTaskLogger.info(msg);
			buffer.append(msg);
			buffer.append("\r\n");
		}
	}
	
	static public String getFormMatrix(double startX, double startY, double startZ, 
			double endX, double endY, double endZ,
			double orignDiameter, double orignLength, 
			double tragetDiameter, double targetLength,
			double unitScale
			){
		
		String fromMatrixStr = null;
		
		double[] startPoint = new double[]{startX, startY, startZ};
		double[] endPoint = new double[]{endX, endY, endZ};
		
		XFormMatrixUtil aXFormMatrixUtil = new XFormMatrixUtil();
		
		double[] rotationAngles = aXFormMatrixUtil.getCalculatedRotationData(startPoint, endPoint);

		// X�� ����ȸ��
		double xRoattion = rotationAngles[0];		
		// Y�� ����ȸ��
		double yRoattion = rotationAngles[1];
		// Z�� ����ȸ��
		double zRoattion = rotationAngles[2];
		
		//System.out.println("xRoattion = "+xRoattion);
		//System.out.println("yRoattion = "+yRoattion);
		//System.out.println("zRoattion = "+zRoattion);
		//System.out.println("calculatedLength = "+aXFormMatrixUtil.calculatedLength);
		
		// Z�� �߽����� ȸ�� Matrix ���ϱ�
		Transform3D transform = new Transform3D();
		transform.rotZ(Math.toRadians( zRoattion ));

		// Y���� �߽����� ȸ���� Matrix ���ϱ�
		Transform3D transform2nd = new Transform3D();
		//System.out.println("transform2nd(Orign) = "+transform2nd.toString());
		// ��� ������� ȸ�������� �����Ѵ�.
		transform2nd.rotY( Math.toRadians( yRoattion * -1.0d ) );
		//System.out.println("transform2nd(Y Rotate) = "+transform2nd.toString());
		// Y Rotation�� +- 90���� �ʰ� �ϸ� 
		transform.mul(transform2nd);
		//System.out.println("transforma(Y Rotate) = "+transform2nd.toString());
		
		// X�� �������� ���̸� ���̴� Matrix ���� (Scale)
		Transform3D sctransform3d = new Transform3D();
		// ������ Scale ���� ����
		double[] calculatedScales = aXFormMatrixUtil.getCalculatedScaleVector(
				orignDiameter, orignLength, 
				tragetDiameter, targetLength,
				unitScale);
		Vector3d scaleVector = new Vector3d(calculatedScales);
		sctransform3d.setScale(scaleVector);

		// �� Matrix�� ���Ѵ�. (�� ����⿡ ���� Scale ��갪�� ��ģ��.)		
		transform.mul(sctransform3d);
		
//		// �̵��� ���� (��ġ����)
//		Transform3D transform3d = new Transform3D();
//		Vector3d startPositionVector3d = new Vector3d(startX, startY, startZ);
//		//startPositionVector3d = new Vector3d(1.d, 1.d, 1.d);
//		//startPositionVector3d.scale(unitScale);
//		transform3d.setTranslation(startPositionVector3d);
//		transform.mul(transform3d);
		
		// Matrix4d�� ��ȯ�Ѵ�.
		Matrix4d worldMatrix4d = null;
		double[] paramArrayOfDouble = new double[16];
		transform.get(paramArrayOfDouble);
		worldMatrix4d = new Matrix4d(paramArrayOfDouble);
		//System.out.println("worldMatrix4d = "+worldMatrix4d.toString());
		
		// World Matrix�� View Matrix�� ��ȯ �Ѵ�.
		Matrix4d viewMatrix4d = aXFormMatrixUtil.worldMatrixToViewMatrix(worldMatrix4d);
	
		
		Matrix4d reflectionMatrix = aXFormMatrixUtil.getReflectionMatrix();
		//System.out.println("reflectionMatrix = "+reflectionMatrix.toString());
		viewMatrix4d.mul(reflectionMatrix);
		
		viewMatrix4d.m30=startPoint[0];
		viewMatrix4d.m31=startPoint[1];
		viewMatrix4d.m32=startPoint[2];
		
		double resultLength = aXFormMatrixUtil.getCalculatedLength();
		//System.out.println("resultLength = "+resultLength);
		//System.out.println("viewMatrix4d = "+viewMatrix4d.toString().replaceAll(",", "").replaceAll("\\n", " ").trim());
		fromMatrixStr = viewMatrix4d.toString().trim();
		
		return fromMatrixStr;
	}
	
	private double[] getCalculatedRotationData(double[] startPoint, double[] endPoint){
		return getCalculatedRotationDataCase1(startPoint, endPoint);
	}
	
	private double[] getCalculatedRotationDataCase1(double[] startPoint, double[] endPoint){
		
		this.xAxisReflect = false;
		this.yAxisReflect = false;
		this.zAxisReflect = false;
		
		double startX = startPoint[0];
		double startY = startPoint[1];
		double startZ = startPoint[2];
		
		double endX = endPoint[0];
		double endY = endPoint[1];
		double endZ = endPoint[2];
		
		double absLengthX = Math.abs(endX - startX);
		double absLengthY = Math.abs(endY - startY);
		double absLengthZ = Math.abs(endZ - startZ);
		
		double lengthX = endX - startX;
		double lengthY = endY - startY;
		double lengthZ = endZ - startZ;
		
		//System.out.println("absLengthX = "+absLengthX);
		//System.out.println("absLengthY = "+absLengthY);
		//System.out.println("absLengthZ = "+absLengthZ);
		
		double xRotationAngle = 0.0d;
		double yRotationAngle = 0.0d;
		double zRotationAngle = 0.0d;
		
		// Z�� ����
		zRotationAngle = getAngle(absLengthX, absLengthY);
		double rotationAixDiaMeter = Math.sqrt(Math.pow(absLengthX, 2) + Math.pow(absLengthY, 2));
		yRotationAngle = getAngle(rotationAixDiaMeter, absLengthZ);

		// �־��� ������ �Ÿ� ���.
		Point3d tempStartP = new Point3d(startX, startY, startZ);
		Point3d tempEndP = new Point3d(endX, endY, endZ);
		double distance = tempStartP.distance(tempEndP);
		calculatedLength =  distance;
		
		// ������ ���밪�� �������� Rotation�� ��� �����Ƿ� 
		// �� �࿡ ���� Reflection ������ ����� ��� �Ѵ�.		
		if(lengthX < 0){
			// YZ ��鿡 ���� Reflection
			xAxisReflect = true;
		}
		if(lengthY < 0){
			// XZ ��鿡 ���� Reflection
			yAxisReflect = true;
		}
		if(lengthZ < 0){
			// XY ��鿡 ���� Reflection
			zAxisReflect = true;
		}
		
		zRotationAngle = Math.toDegrees(zRotationAngle);
		yRotationAngle = Math.toDegrees(yRotationAngle);

//		System.out.println("rotationAixDiaMeter = "+rotationAixDiaMeter);
//		System.out.println("calculatedLength = "+calculatedLength);
		
		double[] roatationData = new double[] {xRotationAngle, yRotationAngle, zRotationAngle};
		
		return roatationData;
	}
	
	private double getAngle(double width, double height){
		double angle = 0.d;
		
		angle = Math.atan2(height, width);
		
		return angle;
	}
	
	/**
	 * Reflection ���ǿ� ���� Matrix�� ��� �ؼ� Return �Ѵ�. 
	 * �� �Լ��� X,Y,Z �� ���⿡ ���� Reflection ���� Matrix�� Return �Ѵ�. 
	 * @return
	 */
	public Matrix4d getReflectionMatrix(){
		Matrix4d aMatrix4d = new Matrix4d();
		aMatrix4d.setIdentity();
		
		if(this.xAxisReflect==true){
			aMatrix4d.m00 = (aMatrix4d.m00 * -1.d);
		}
		if(this.yAxisReflect==true){
			aMatrix4d.m11 = (aMatrix4d.m11 * -1.d);
		}
		if(this.zAxisReflect==true){
			aMatrix4d.m22 = (aMatrix4d.m22 * -1.d);
		}
		
		return aMatrix4d;
	}
	
	/**
	 * Co2 ���� ������ ������ ���̸� �������� ���ϴ� ������ ������ ������ �ǵ���
	 * Scale�� �����ϴ� ����� �����ϴ� �Լ�
	 * @param orignDiameter Co2 �������� ǥ���ϴ� ������ ������ ������ (mm) ������ 
	 * @param orignLength Co2 �������� ǥ���ϴ� ������ ������ ���� (mm) ������ 
	 * @param targetDiameter Co2 �������� ǥ���� ������ �������� ���ϴ� �� (mm) ������
	 * @param targetLength Co2 �������� ǥ���� ������ ���̷� ���ϴ� �� (mm) ������, 0�� �Է��ϴ°�� ���� ���� ������
	 * @param unitScale Structure�� ������ ��ȯ�� ���� ���� Scale ��
	 * @return x,y,z ���⿡ ����� Scale ���� ���� double[]�� ��ȯ�Ѵ�.
	 */
	private double[] getCalculatedScaleVector(double orignDiameter, double orignLength,
			double targetDiameter, double targetLength, 
			double unitScale 
			){

		// x�� ���̴� �Էµ� ���� 0�� �ƴѰ�� �־��� ���� ���̰� �ǵ��� �ϰ�
		// �Էµ� ���� 0�� ��� ���� ���� ����Ѵ�.
		double xScale = 1;
		if(targetLength==0.0d){
			xScale = (calculatedLength/orignLength) * unitScale;
		}else{
			xScale = (targetLength/orignLength) * unitScale;
		}
		double yScale = (targetDiameter/orignDiameter) * unitScale;
		double zScale = (targetDiameter/orignDiameter) * unitScale;
		
		double[] calculatedScale = new double[] {xScale, yScale, zScale};
		
		return calculatedScale;
	}
	
	public double getCalculatedLength() {
		return calculatedLength;
	}

	public boolean isxAxisReflect() {
		return xAxisReflect;
	}

	public boolean isyAxisReflect() {
		return yAxisReflect;
	}

	public boolean iszAxisReflect() {
		return zAxisReflect;
	}

	
	/**
	 * World Matrix�� View Matrix�� ��ȯ �Ѵ�.
	 * ������ ���꿡 ���� Matrix�� Teamcenter�� XFormMatrix�� ���Ǵ� View Matrix�� ��ȯ�Ѵ�.
	 * @param worldMatrix
	 * @return
	 */
	private Matrix4d worldMatrixToViewMatrix(Matrix4d worldMatrix ){
		Matrix4d viewMatrix = new Matrix4d();
		
		viewMatrix = worldMatrix;
		viewMatrix.transpose();
		
//		viewMatrix.m00 = worldMatrix.m00;
//		viewMatrix.m01 = worldMatrix.m10;
//		viewMatrix.m02 = worldMatrix.m20;
//		viewMatrix.m03 = worldMatrix.m30;
//		
//		viewMatrix.m10 = worldMatrix.m01;
//		viewMatrix.m11 = worldMatrix.m11;
//		viewMatrix.m12 = worldMatrix.m21;
//		viewMatrix.m13 = worldMatrix.m31;
//		
//		viewMatrix.m20 = worldMatrix.m02;
//		viewMatrix.m21 = worldMatrix.m12;
//		viewMatrix.m22 = worldMatrix.m22;
//		viewMatrix.m33 = worldMatrix.m23;
//		
//		viewMatrix.m30 = worldMatrix.m03;
//		viewMatrix.m31 = worldMatrix.m13;
//		viewMatrix.m32 = worldMatrix.m23;
//		viewMatrix.m33 = worldMatrix.m33;
		
		return viewMatrix;
	}
}
