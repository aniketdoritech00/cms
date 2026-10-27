package in.doritech.cms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "param")
public class ParamEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "param_id")
	private Integer paramId;

	@Column(name = "code", length = 50)
	private String code;

	@Column(name = "serial", length = 50)
	private String serial;

	@Column(name = "serial_no")
	private Integer serialNo;

	@Column(name = "desp1", nullable = false, length = 255, unique = true)
	private String desp1;

	@Column(name = "desp2", nullable = false, length = 255)
	private String desp2;

	@Column(name = "desp3", length = 255)
	private String desp3;

	@Column(name = "desp4", length = 255)
	private String desp4;

	@Column(name = "desp5", length = 255)
	private String desp5;

	// Getters and Setters

	/**
	 * @return the desp4
	 */
	public String getDesp4() {
		return desp4;
	}

	/**
	 * @return the serialNo
	 */
	public Integer getSerialNo() {
		return serialNo;
	}

	/**
	 * @param serialNo
	 *            the serialNo to set
	 */
	public void setSerialNo(Integer serialNo) {
		this.serialNo = serialNo;
	}

	/**
	 * @param desp4
	 *            the desp4 to set
	 */
	public void setDesp4(String desp4) {
		this.desp4 = desp4;
	}

	/**
	 * @return the desp5
	 */
	public String getDesp5() {
		return desp5;
	}

	/**
	 * @param desp5
	 *            the desp5 to set
	 */
	public void setDesp5(String desp5) {
		this.desp5 = desp5;
	}

	public Integer getParamId() {
		return paramId;
	}

	public void setParamId(Integer paramId) {
		this.paramId = paramId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return the serial
	 */
	public String getSerial() {
		return serial;
	}

	/**
	 * @param serial
	 *            the serial to set
	 */
	public void setSerial(String serial) {
		this.serial = serial;
	}

	public String getDesp1() {
		return desp1;
	}

	public void setDesp1(String desp1) {
		this.desp1 = desp1;
	}

	public String getDesp2() {
		return desp2;
	}

	public void setDesp2(String desp2) {
		this.desp2 = desp2;
	}

	public String getDesp3() {
		return desp3;
	}

	public void setDesp3(String desp3) {
		this.desp3 = desp3;
	}
}