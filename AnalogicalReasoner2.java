package codelet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

public class AnalogicalReasoner2 extends Codelet{
	public static int nextChunkID = 0;
	public class Chunk{
		public String name;
		public int ID;
		public double SRA,RRA,activation,tolerance;
		public ArrayList<DVPair> dvPairs;
		public Chunk(String name){
			this.name = name;
			ID = nextChunkID;
			nextChunkID += 1;
			dvPairs = new ArrayList<DVPair>();
			SRA = 0.0;
			RRA = 0.0;
			activation = 0.0;
			tolerance = 0.75;
		}
		public void addDVPair(DVPair dv){
			if(!dvPairs.contains(dv)){
				dvPairs.add(dv);
			}
		}
		public String toString(){
			return this.name + "[" + ID + "]";
		}
		public boolean equals(Chunk other){
			return other.name.equals(this.name);
		}
	}
	public class DVPair{
		public String name,dimension,value;
		public double activation;
		public ArrayList<Chunk> chunks;
		//TODO: implement the default value options for the constructor
		public DVPair(String name, String dimension, String value){
			this.name = name;
			this.dimension = dimension;
			this.value = value;
			this.activation = 0;
			chunks = new ArrayList<Chunk>();
		}
		public DVPair(String name){
			this.name = name;
			this.dimension = randString(100);
			this.value = randString(100);
			this.activation = 0;
			chunks = new ArrayList<Chunk>();
		}
		public void addChunk(Chunk newChunk){
			if(!chunks.contains(newChunk)){
				chunks.add(newChunk);
			}
		}
		public String toString(){
			return name;
		}
	}
	public class AssociativeRule{
		public ArrayList<Chunk> conditionChunks;
		public Chunk conclusionChunk;
		public ArrayList<Double> conditionWeights;
		public AssociativeRule(ArrayList<Chunk> conditionChunks, Chunk conclusionChunk,ArrayList<Double> conditionWeights){
			this.conditionChunks = conditionChunks;
			this.conclusionChunk = conclusionChunk;
			if(conditionWeights.size() == 0){
				conditionWeights = new ArrayList<Double>();
				for(int i = 0; i < conditionChunks.size(); i++){
					conditionWeights.add(1.0);
				}
			}
			else{
				this.conditionWeights = conditionWeights;
			}
		}
		public String toString(){
			String s = "Condition: {";
			for(Chunk c : conditionChunks){
				s += c.toString() + ',';
			}
			s += "}; Conclusion: " + conclusionChunk.toString();
			return s;
		}
	}
	public class NACS{
		HashMap<String,DVPair> AMN;
		HashMap<String,ArrayList<Chunk>> GKS;
		ArrayList<AssociativeRule> associativeRules;
		ArrayList<Chunk> NTH,CDCs;
		Chunk PRED,PARENT,CHILD,TEMPLATE,LINKER,TYPE,ORDER,TOP_PROP;
		final double DEACTIVATEDVALUE = 0.1;
		double alpha, beta, similarityTolerance, decay;
		public NACS(){
			
			AMN = new HashMap<String,DVPair>();
			GKS = new HashMap<String,ArrayList<Chunk>>();
	        associativeRules = new ArrayList<AssociativeRule>();
	        resetParameters();
	        //TODO: list of templates? self.templates = []
	        //TODO: what does this do self.storedSemantics = dict() #used temporarily to store some labels and the semantics within
	        //PRED = new Chunk("PRED");
	        PARENT = new Chunk("PARENT");
	        CHILD = new Chunk("CHILD");
	        TEMPLATE = new Chunk("TEMPLATE");
	        LINKER = new Chunk("LINKER");
	        //TYPE = new Chunk("TYPE");
	        //ORDER = new Chunk("ORDER");
	        NTH = new ArrayList<Chunk>();
	        for(int i = 0; i < 10; i++){
	        	NTH.add(new Chunk("NTH"+ i));
	        	addNewChunk(NTH.get(i));
	        }
	        TOP_PROP = new Chunk("TOP_PROP");
	        addNewChunk(TOP_PROP);
	        addNewChunk(PARENT);
	        addNewChunk(CHILD);
	        addNewChunk(LINKER);
	        addNewChunk(TEMPLATE);
	        CDCs = new ArrayList<Chunk>();
	        CDCs.add(PARENT);CDCs.add(CHILD);CDCs.add(TEMPLATE);CDCs.add(TOP_PROP);CDCs.add(LINKER);
	        for(Chunk c : NTH){
	        	CDCs.add(c);
	        }
		}
		
		public void resetParameters(){
			alpha = 1.0;
			beta = 1.0;
			similarityTolerance = .35;
			decay = 0.0;
		}
		
		public void deactivateChunk(Chunk c){
			c.activation = DEACTIVATEDVALUE;
		}
		public DVPair addNewDVPair(DVPair newDVPair){
			
			if(AMN.keySet().contains(newDVPair.name)){
				return AMN.get(newDVPair.name);
			}
			else{
				AMN.put(newDVPair.name, newDVPair);
				return newDVPair;
			}
		}
		public void addNewChunk(Chunk newChunk){
			GKS.get(newChunk.name).add(newChunk);
		}
		
		public void removeChunks(ArrayList<Chunk> toRemove){
			ArrayList<AssociativeRule> rulesToRemove = new ArrayList<AssociativeRule>();
			for(AssociativeRule a : associativeRules){
				//TODO: implement intersection rule if intersection
					rulesToRemove.add(a);
			}
			for(AssociativeRule a : rulesToRemove){
				removeAssociativeRule(a.conditionChunks,a.conclusionChunk);
			}
			for(Chunk c : toRemove){
				for(DVPair d : c.dvPairs){
					d.chunks.remove(c);
				}
				//TODO: verify this works lmao
				GKS.get(c.name).remove(GKS.get(c.name).indexOf(c));
			}
		}
		public void linkChunks(Chunk c1, Chunk c2){
			ArrayList<Chunk> chunks1 = new ArrayList<Chunk>();
			chunks1.add(c1); chunks1.add(LINKER);
			ArrayList<Chunk> chunks2 = new ArrayList<Chunk>();
			chunks2.add(c2); chunks2.add(LINKER);
			ArrayList<Double> weights1 = new ArrayList<Double>();
			weights1.add(.5); weights1.add(.5);
			ArrayList<Double> weights2 = new ArrayList<Double>();
			weights2.add(.5); weights2.add(.5);
			try{
				addAssociativeRule(new AssociativeRule(chunks1,c2,weights1));
				addAssociativeRule(new AssociativeRule(chunks2,c1,weights2));
			}catch(Exception e){
				System.out.println(e);
			}
		}
		public void connectChunkDVPair(Chunk c, DVPair d){
			c.addDVPair(d);
			d.addChunk(c);
		}
		public ArrayList<Chunk> getAllChunks(){
			ArrayList<Chunk> toReturn = new ArrayList<Chunk>();
			for(Entry<String,ArrayList<Chunk>> e : GKS.entrySet()){
				for(Chunk c : e.getValue()){
					toReturn.add(c);
				}
			}
			return toReturn;
		}
		
		public AssociativeRule addAssociativeRule(AssociativeRule newRule) throws Exception{
			
			for(Chunk c : newRule.conditionChunks){
				if(!GKS.containsKey(c.name)){
					throw new Exception("No condition chunk named " + c.name + " in GKS!");
				}
			}
			if(!GKS.containsKey(newRule.conclusionChunk.name)){
				throw new Exception("No conclusion chunk named " + newRule.conclusionChunk.name + " in GKS!");
			}
			associativeRules.add(newRule);
			return newRule;
		}
		

		public AssociativeRule removeAssociativeRule(ArrayList<Chunk> conditionChunks, Chunk conclusionChunk){
			AssociativeRule toReturn = null;
			for(AssociativeRule r : associativeRules){
				if(r.conclusionChunk.equals(conclusionChunk)){
					ArrayList<Chunk> con = new ArrayList<Chunk>();
					for(Chunk c : r.conditionChunks){
						con.add(c);
					}
					boolean match = true;
					for(Chunk c : conditionChunks){
						if(con.contains(c)){
							con.remove(c);
						}
						else{
							match = false;
							break;
						}
					}
					if(!con.isEmpty()){
						match = false;
					}
					if(match){
						toReturn = r;
						associativeRules.remove(r);
						break;
					}
				}
			}
			return toReturn;
		}
		//TODO: also a hard function, this needs to be re-evaluated
		//since we don't know the type of stringStruct- may have undefined behavior
		public String propStructToString(List<String> stringStruct){
			String argSeparator,nestedStart,nestedEnd;
			argSeparator = "__@#__";
			nestedStart = "__*(*__";
			nestedEnd = "__*)*__";
			String toReturn = null;
	        if(stringStruct.size() == 1){
	        	return stringStruct.get(0);
	        }
	        else{
	        	toReturn = nestedStart + stringStruct.get(0) + argSeparator;
                for(int i = 1; i < stringStruct.size(); i++){
                	toReturn = toReturn + propStructToString(stringStruct.subList(i, i)) + argSeparator;
                }
                    
                toReturn = toReturn + nestedEnd;
	        }
            
	        return toReturn;
		}
		//TODO: needs implementation fixes probably.. 
		public ArrayList<String> parseTokens(ArrayList<Character> tokens) throws Exception{
			if(tokens.size() <= 1){
				throw new Exception("Passed end of token list!");
			}
			if(!tokens.get(0).equals(')')){
				throw new Exception("Called parseTokens on non-rooted node!");
			}
			char pred = tokens.get(1);
			if(pred == ')' || pred == '('){
				throw new Exception("No predicate found!");
			}
			tokens.remove(0);
			tokens.remove(0);
			ArrayList<String> toReturn = new ArrayList<String>();
			toReturn.add("" + pred);
			while(true){
				if(tokens.size() == 0){
					throw new Exception("Reached end of tokens while parsing!");
				}
				if(tokens.get(0) == ')'){
					tokens.remove(0);
					return toReturn;
				}
				else if(tokens.get(0) == '('){
					ArrayList<String> arg = parseTokens(tokens);
					for(String s : arg){
						toReturn.add(s);
					}
				}
				else{
					toReturn.add("" + tokens.get(0));
					tokens.remove(0);
				}
			}
		}
		
		public void addFiveDVPairs(Chunk c){
			for(int i = 0; i < 5; i++){
				DVPair d = new DVPair(c.name + "__" + i);
				addNewDVPair(d);
				connectChunkDVPair(c,d);
			}
		}
		//TODO: do this later-it's a lot
		//overloaded for handling String input and ArrayList<String> input
		public void addExpression_internal(String structure) throws Exception{
			if(structure.length() < 1){
				throw new Exception("String is empty!");
			}
			String propName = structure;
			boolean propIsLinked = propName.charAt(0) == '\'';
			boolean propIsBlank = propName.charAt(0) == '_';
			if(propIsBlank && propName.length() > 1){
				propIsLinked = true;
			}
			if(propIsLinked && !propIsBlank){
				propName = propName.substring(1, propName.length());
			}
			String propLongName = propStructToString(structure);
			ArrayList allArgNodes;
			ArrayList allNonArgNodes;
			for(int i = 1; i < structure.length(); i++){
				
			}
			
		}
		public void addExpression_internal(ArrayList<String> structure) throws Exception{
			
		}
		//TODO: verify the regex works properly and we get the right splitting
		public addExpression(String expr, boolean simple){
			//Something about linkedsymbols
			HashMap<x,x> linkedSymbols;
			String[] tokensAll = expr.split("#.*[\n\r]|[a-zA-Z0-9-_'!]+|(|)");
			ArrayList<String> tokens = new ArrayList<String>();
			for(String s : tokensAll){
				if(s.charAt(0) != '#'){
					tokens.add(s.toUpperCase());
				}
			}
			if(!simple){
				parseTokens(tokens);
			}
			else{
				
			}
		}
		//TODO: everything from this function to addExpression is fuzzy...
		//will need fixing for sure
		public void addTemplate(String expr, String templateName){
			if(templateName == null){templateName = "defaultTemplateName";}
			HashMap linkedSymbols;
			HashMap chunkWeights;
			String[] tokensAll = expr.split("#.*[\n\r]|[a-zA-Z0-9-_'!]+|(|)");
			ArrayList<String> tokens = new ArrayList<String>();
			for(String s : tokensAll){
				if(s.charAt(0) != '#'){
					tokens.add(s.toUpperCase());
				}
			}
			ArrayList<String> structuredList = parseTokens(tokens);
			while(!tokens.isEmpty()){
				ArrayList<String> temp = parseTokens(tokens);
				for(String s : temp){
					structuredList.add(s);
				}
			}
			ArrayList templateChunks;
			for(String s : structuredList){
				
			}
		}
		public String writeExpressions_internal(Chunk root){
			ArrayList<Chunk> children = new ArrayList<Chunk>();
			
			
			for(Chunk CDC : NTH){
				//TODO: think of a better way to do this args stuff
				ArrayList<Chunk> args = new ArrayList<Chunk>();
				args.add(root); args.add(CDC);
				children.add(pulseAndCheck(args));
			}
			if(children.size() == 0){
				return root.name + '[' + root.ID + ']';
			}
			else{
				String toReturn = "(" + root.name + '[' + root.ID + "] ";
				for(Chunk c : children){
					toReturn += writeExpressions_internal(c) + " ";
				}
				return toReturn;
			}
		}
		public void writeExpressions(ArrayList<String> doNotPrint){
			if(doNotPrint == null){
				doNotPrint = new ArrayList<String>();
			}
			System.out.println("TOP-LEVEL PROPOSITIONS:");
			String toPrint = pulseAndCheck(TOP_PROP);
			for()
		}
		public ArrayList<Chunk> pulseAndCheck(ArrayList<Chunk> activeChunks, double minActivation){
			//TODO: the minActivation is defaulted to be 1.0 if not supplied
			minActivation = 1.0;
			HashMap<Chunk,Double> activated = new HashMap<Chunk,Double>();
			for(AssociativeRule a : associativeRules){
				double thisRRA = 0;
				for(int i = 0; i < a.conditionChunks.size(); i++){
					if(activeChunks.contains(a.conditionChunks.get(i))){
						thisRRA += a.conditionWeights.get(i);
					}
				}
				//TODO: check if we're emulating defaultdict behavior properly
				if(thisRRA >= a.conclusionChunk.tolerance){
					if(activated.containsKey(a.conclusionChunk)){
						double prevValue = activated.get(a.conclusionChunk);
						activated.put(a.conclusionChunk,prevValue + thisRRA);
					}
					else{
						activated.put(a.conclusionChunk, thisRRA);
					}
				}
			}
			ArrayList<Chunk> toReturn = new ArrayList<Chunk>();
			for(Entry<Chunk,Double> c : activated.entrySet()){
				if(c.getValue() >= c.getKey().tolerance){
					toReturn.add(c.getKey());
				}
			}
			return toReturn;
		}
		public void resetActivations(boolean totalDeactivation){
			//TODO: this is a default value of false
			totalDeactivation = false;
			double v = DEACTIVATEDVALUE; 
			if(totalDeactivation == true){v = 0;}
			for(Entry<String,ArrayList<Chunk>> k : GKS.entrySet()){
				for(Chunk c : k.getValue()){
					c.RRA = 0;
					c.SRA = 0;
					if(totalDeactivation){
						c.activation = 0;
					}
					else{
						if(c.activation >= DEACTIVATEDVALUE){
							c.activation = DEACTIVATEDVALUE;
						}
						else{
							c.activation = 0;
						}
					}
				}
			}
			for(DVPair d : AMN.values()){
				d.activation = 0;
			}
		}
		public ArrayList<Chunk> getChunks(String name){
			ArrayList<Chunk> toReturn = new ArrayList<Chunk>();
			for(Chunk c : GKS.get(name.toUpperCase())){
				toReturn.add(c);
			}
			return toReturn;
		}
		public ArrayList<Chunk> getChunks(List<String> names){
			ArrayList<Chunk> toReturn = new ArrayList<Chunk>();
			for(String name : names){
				for(Chunk c : GKS.get(name.toUpperCase())){
					toReturn.add(c);
				}
			}
			return toReturn;
		}
		public void performRBR(){
			for(AssociativeRule r : associativeRules){
				double thisRRA = 0;
				for(int i = 0; i < r.conditionChunks.size(); i++){
					thisRRA += r.conditionChunks.get(i).activation * r.conditionWeights.get(i);
				}
				if(thisRRA < r.conclusionChunk.tolerance){
					thisRRA = 0;
				}
				r.conclusionChunk.RRA = Math.max(r.conclusionChunk.RRA, thisRRA);
			}
		}
		public void performSBR(){
			for(Entry<String,ArrayList<Chunk>> k1 : GKS.entrySet()){
				for(Chunk c1 : k1.getValue()){
					for(Entry<String,ArrayList<Chunk>> k2 : GKS.entrySet()){
						for(Chunk c2 : k2.getValue()){
							if(c1.activation == 0 || c1 == c2){
								continue;
							}
							double sim = getSimilarity(c1,c2);
							if(sim >= similarityTolerance){
								c2.SRA = Math.max(c2.SRA, sim*c1.activation);
							}
						}
					}
				}
			}
		}
		public double getSimilarity(Chunk c1, Chunk c2){
			if(c1 == c2){
				return 1;
			}
			if(c2.dvPairs.size() == 0){
				return 1;
			}
			int numOverlap = 0;
			for(DVPair d1 : c1.dvPairs){
				for(DVPair d2 : c2.dvPairs){
					//TODO: check what kind of similarity we need (== or .equals)
				}
			}
		}
	}
	//Unimplemented at the moment
	public void kill(){
		//TODO: implement me!
	}
	public void run(){
		//TODO: implement me!
	}
	//Returns a randomly constructed string of input size
	private static String randString(int size){
		Random rand = new Random();
		String s = "rGen";
		for(int i = 0; i < size; i++){
			s += (char)(rand.nextInt(74)+48);
		}
		return s;
	}
	
}
