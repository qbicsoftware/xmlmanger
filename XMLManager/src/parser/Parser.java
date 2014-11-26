package parser;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import xml.Factor;
import xml.Qcategorical;
import xml.Qcontinous;
import xml.Qfactors;
import xml.Qproperties;
import xml.Qproperty;


public class Parser {

  public Map<String, String> getMap(JAXBElement<xml.Qproperties> root) {
    Map<String, String> map = new HashMap<String, String>();
    if (root.getValue().getQfactors() != null) {
      Qfactors factors = root.getValue().getQfactors();
      for (Qcategorical cat : factors.getQcategorical()) {
        map.put(cat.getLabel(), cat.getValue());
      }
      for (Qcontinous cont : factors.getQcontinous()) {
        map.put(cont.getLabel(), cont.getValue() + " " + cont.getUnit());
      }
    }
    if (root.getValue().getQproperty() != null) {
      List<Qproperty> props = root.getValue().getQproperty();
      for (Qproperty prop : props) {
        map.put(prop.getLabel(), prop.getValue() + " " + prop.getUnit());
      }
    }
    return map;
  }
  
  public List<Factor> getFactors(JAXBElement<xml.Qproperties> root) {
    List<Factor> res = new ArrayList<Factor>();
    if (root.getValue().getQfactors() != null) {
      Qfactors factors = root.getValue().getQfactors();
      for (Qcategorical cat : factors.getQcategorical()) {
        res.add(new Factor(cat.getLabel(), cat.getValue()));
      }
      for (Qcontinous cont : factors.getQcontinous()) {
        res.add(new Factor(cont.getLabel(), cont.getValue().toString(), cont.getUnit()));
      }
    }
    if (root.getValue().getQproperty() != null) {
      List<Qproperty> props = root.getValue().getQproperty();
      for (Qproperty prop : props) {
        res.add(new Factor(prop.getLabel(), prop.getValue().toString(), prop.getUnit()));
      }
    }
    return res;
  }

  public String addFactorsToXMLString(String xml, List<Factor> factors) throws JAXBException {
    return toString(addFactors(parseXMLString(xml), factors));
  }

  public JAXBElement<xml.Qproperties> addFactors(JAXBElement<xml.Qproperties> root,
      List<Factor> factors) {
    if (root.getValue().getQfactors() == null)
      root.getValue().setQfactors(new Qfactors());
    Qfactors factorRoot = root.getValue().getQfactors();
    List<Qcategorical> cats = factorRoot.getQcategorical();
    List<Qcontinous> cont = factorRoot.getQcontinous();
    for (Factor f : factors) {
      String label = f.getLabel();
      if (f.hasUnit()) {
        Qcontinous q = new Qcontinous();
        String str = f.getValue().replaceAll(",", "");
        BigDecimal value = new BigDecimal(str);
        q.setLabel(label);
        q.setValue(value);
        q.setUnit(f.getUnit());
        cont.add(q);
      } else {
        Qcategorical q = new Qcategorical();
        q.setLabel(label);
        q.setValue(f.getValue());
        cats.add(q);
      }
    }
    return root;
  }

  public List<Factor> getFactorsFromXML(String xml) throws JAXBException {
    List<Factor> res = new ArrayList<Factor>();
    Qproperties props = parseXMLString(xml).getValue();
    if (props != null) {
      Qfactors fact = props.getQfactors();
      if (fact != null) {
        for (Qcategorical cat : fact.getQcategorical())
          res.add(new Factor(cat.getLabel(), cat.getValue(), ""));
        for (Qcontinous cont : fact.getQcontinous())
          res.add(new Factor(cont.getLabel(), cont.getValue().toString(), cont.getUnit()));
      }
      List<Qproperty> pLis = props.getQproperty();
      if (pLis != null) {
        for (Qproperty prop : pLis) {
          res.add(new Factor(prop.getLabel(), prop.getValue(), prop.getUnit()));
        }
      }
    }
    return res;
  }

  public JAXBElement<Qproperties> createXMLFromFactors(List<Factor> factors) throws JAXBException {
    return addFactors(getEmptyXML(), factors);
  }

  public JAXBElement<xml.Qproperties> parseXMLString(String xml) throws JAXBException {
    JAXBContext jc = JAXBContext.newInstance("xml");
    Unmarshaller unmarshaller = jc.createUnmarshaller();
    JAXBElement<Qproperties> root =
        unmarshaller.unmarshal(new StreamSource(new StringReader(xml)), Qproperties.class);
    return root;
  }

  public JAXBElement<xml.Qproperties> getEmptyXML() throws JAXBException {
    JAXBContext jc = JAXBContext.newInstance("xml");
    Unmarshaller unmarshaller = jc.createUnmarshaller();
    JAXBElement<Qproperties> root =
        unmarshaller.unmarshal(new StreamSource(new StringReader(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + "<qproperties>"
                + "</qproperties>")), Qproperties.class);
    return root;
  }

  public static void main(String[] args) throws JAXBException {
    Parser p = new Parser();
    p.toString(p.createXMLFromFactors(new ArrayList<Factor>(Arrays.asList(new Factor("label",
        "20.235", "y"), new Factor("label", "cancer", ""), new Factor("label", "120.235", "mg")))));
  }

  public String toString(JAXBElement<xml.Qproperties> root) throws JAXBException {
    JAXBContext jc = JAXBContext.newInstance("xml");
    Marshaller marshaller = jc.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    StringWriter sw = new StringWriter();
    marshaller.marshal(root, sw);
    return sw.toString();
  }

}