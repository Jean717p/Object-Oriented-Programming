package schools;

import it.polito.utility.files.CsvParser;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;
import static java.util.Comparator.*;


public class Region {
	private String nome;
	private Map<String,Community> commap = new HashMap<>();
	private Map<String,Municipality> munmap = new HashMap<>();
	private Map<String,School> schmap = new HashMap<>();
	
	public Region(String name){
		this.nome=name;
		return;
	}
	
	public String getName(){
		return this.nome;
	}
	
	public Collection<School> getSchools() {
		return schmap.values();
	}
	
	public Collection<Community> getCommunities() {
		return commap.values();
	}
	
	public Collection<Municipality> getMunicipalies() {
		return munmap.values();
	}
	
	
	// factory methods
	
	public Community newCommunity(String name, Community.Type type){
		Community e=new Community(name,type);
		commap.put(name, e);
		return e;
	}

	public Municipality newMunicipality(String nome, String provincia, String codiceAF, String nomeAF){
		Municipality e=new Municipality(nome,provincia,codiceAF,nomeAF,null);
		munmap.put(nome, e);
		return e;
	}
	public Municipality newMunicipality(String nome, String provincia, String codiceAF, String nomeAF,Community comunita){
		Municipality e=new Municipality(nome,provincia,codiceAF,nomeAF,comunita);
		munmap.put(nome, e);
		return e;
	}
	
	public School newSchool(String name, String code, int grade,
			String description, String managementType, String legalPosition){
		School e=new School(name,code,grade,description,managementType,legalPosition);
		schmap.put(code, e);
		return e;
	}
	
	public Branch newBranch(int regionalCode, String branchType, Municipality municipality, String indirizzo, int cap,
							String locality, String telephone, String fax,School school)	{
		return new Branch(regionalCode,branchType,municipality,indirizzo,cap,locality,telephone,fax,school);
	}
	
	public void readData(String url) throws IOException{
		CsvParser parser = CsvParser.createInstance();
		Stream<List<String>> tmpmap=parser.openRowsUrl(url);
		tmpmap.forEach(e->read(e));
		return;
	}
	private void read(List<String> L){
		String noC="NESSUNA COMUNITA' COLLINARE";
		String noM="NESSUNA COMUNITA' MONTANA";
		Community cmty=null;
		Municipality munc;
		School schtmp;
		if(false==L.get(13).equals(noC)){
			if(commap.containsKey(L.get(13))==false){
				cmty=newCommunity(L.get(13),Community.Type.COLLINARE);
			}
		}
		else if(false==L.get(14).equals(noM)){
			if(commap.containsKey(L.get(14))==false){
				cmty=newCommunity(L.get(14),Community.Type.MONTANA);
			}
		}
		if(munmap.containsKey(L.get(1))==false){
			munc=newMunicipality(L.get(1),L.get(0),L.get(15),L.get(16),cmty);
		}
		else{
			munc=munmap.get(L.get(1));
		}
		if(schmap.containsKey(L.get(5))==false){
			char y=L.get(2).charAt(0);
			schtmp=newSchool(L.get(6),L.get(5),(int)(y-48),L.get(3),L.get(17),L.get(18));
		}
		else{
			schtmp=schmap.get(L.get(5));
		}
		Branch x=newBranch(Integer.parseInt(L.get(4)),L.get(7),munc,L.get(8),Integer.parseInt(L.get(9)),
							L.get(10),L.get(12),L.get(11),schtmp);
		return;
	}


	public Map<String,Long>countSchoolsPerDescription(){
		return (Map<String, Long>) schmap.
				values().stream()
				.collect(
						groupingBy(School::getDescription,counting())
						);
	}

	
	public Map<String,Long>countBranchesPerMunicipality(){
		return schmap.values().stream().map(School::getBranches).flatMap(Collection::stream)
				.collect(groupingBy(e->e.getMunicipality().getName(),counting()));
		// return munmap.values().stream().collect(Collectors.toMap(e->e.getName(), e->e.total()));
	}

	public Map<String,Double>averageBranchesPerMunicipality(){
		return munmap.values().stream()
				.collect(groupingBy(Municipality::getProvince,
								averagingInt((Municipality m) -> m.getBranches().size())
								))
				;
	}


	public Collection<String> countSchoolsPerMunicipality(){
		Map<String,Set<School>> step1 =
				schmap.values().stream().flatMap( s -> s.getBranches().stream())
				.collect(groupingBy( b -> b.getMunicipality().getName(),
						mapping(Branch::getSchool, toSet())
						))
						;
		
		return step1.entrySet().stream() 
				.map( e -> e.getValue().size() + " - " + e.getKey())
				.collect(toList())
				;
	}
	

	public List<String> countSchoolsPerCommunity(){
		Map<String,Set<School>> step1 =
				schmap.values().stream().flatMap( s -> s.getBranches().stream())
				.filter( b -> b.getMunicipality().getCommunity().isPresent() )
				.collect(groupingBy( b -> b.getMunicipality().getCommunity().get().getName(),
						mapping(Branch::getSchool, toSet())
						))
						;
		
		return step1.entrySet().stream() 
				.map( e -> e.getValue().size() + " - " + e.getKey())
				.collect(toList())
				;
	}

}
