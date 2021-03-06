package com.mainbot.main;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;

import com.mainbot.components.CategoryDefinition;
import com.mainbot.components.Edit;
import com.mainbot.components.HTMLVisualization;
import com.mainbot.dataobjects.Revision;
import com.mainbot.utility.ConnectionRequests;
import com.mainbot.utility.Constants;
import com.mainbot.utility.Utils;

public class NewsLetterBot extends Bot{
	
	String sessionID;
	String username;
	String password;

	/*Bot for creating a newsletter from last week on the wiki.
	 * On the page /newsletter
	 * */
	NewsLetterBot(String username) 
	{
		super(username);
	}
	
	NewsLetterBot(String username, String password) 
	{
		super(username, password);
	}
	

	
	public static void createNewsletter(NewsLetterBot mainbot, String whichPage) throws JSONException, IOException, ParseException
	{
		CategoryDefinition catDef = new CategoryDefinition();
		
		int dataset = catDef.countArticlesOfCategoryNDays("Category:Dataset_(L)", 7);
		int user = catDef.countArticlesOfCategoryNDays("Category:Person_(L)", 7);
		//int publication = catDef.countArticlesOfCategoryNDays("Category:Publication_(L)", 7);
		int workingGroup = catDef.countArticlesOfCategoryNDays("Category:Working_Group", 7);
		catDef.countArticlesOfCategory("Category:Working_Group");//this checks all updated wrking groups
		
		ArrayList<String> datasetLinks = catDef.datasetLinks;
		//ArrayList<String> publicationLinks = catDef.publicationLinks;
		ArrayList<String> otherPageLinks = catDef.otherLinks;
		
		ArrayList<String> datasetLinksRaw = catDef.datasetLinksRaw;
		//ArrayList<String> publicationLinksRaw = catDef.publicationLinksRaw;
		ArrayList<String> otherPageLinksRaw = catDef.otherLinksRaw;
		
		ArrayList<String> mostActiveUserAndHisContribNum = catDef.getMostActiveUserAndHisContribs();
		
		ArrayList<String> revisedWorkingGroupLinks = catDef.revisedWorkingGroupLinks;
		ArrayList<String> revisedWorkingGroupLinksRaw = catDef.revisedWorkingGroupLinksRaw;
		ArrayList<Integer> revisedWorkingGroupLinksNum = catDef.revisedWorkingGroupLinksNum;
		
		HTMLVisualization view = new HTMLVisualization();
		Edit edit = new Edit();
		
		int deleteLastRevId = catDef.getLastRevisionId(whichPage);
		//System.out.println("Last REVID is: " + deleteLastRevId);
		edit.undoRevisions(deleteLastRevId, false, mainbot, whichPage);//remove previous newsletter
		
		
		view.newsletter(dataset, user, workingGroup, datasetLinks, datasetLinksRaw, 
				otherPageLinks, otherPageLinksRaw, 
				revisedWorkingGroupLinks, revisedWorkingGroupLinksRaw, revisedWorkingGroupLinksNum, mostActiveUserAndHisContribNum, 7);
		
		int revid = edit.edit(view, mainbot, whichPage);
		//undoRevisions for testing use
//		System.out.println("THE REVID IS: " + revid);
//		Scanner scanner = new Scanner(System.in);
//		String erase = scanner.next();
//		if(!erase.isEmpty())
//		{
//			System.out.println("ENTERING IF ERASE CLAUSE!!");
//			edit.undoRevisions(revid, false, mainbot); //testing purpose to undo the edits
//			System.out.println("ERASING FINISHED!!");
//		}
		
		
	}
	
    public static int getPageId(String whichPage) throws JSONException
    {
    	String url = "http://wiki.linked.earth/wiki/api.php?action=query&prop=pageprops&titles=" + whichPage + "&format=json";
    	JSONObject art = ConnectionRequests.doGETJSON(url);

    	//System.out.println(art);
    	
    	//System.out.println(art.getJSONObject("query").getJSONObject("pages")   );
    	JSONObject pages = art.getJSONObject("query").getJSONObject("pages");
    	//pages are in this format:
    	//{"33579":{"ns":0,"pageid":33579,"title":"Weekly Summary","pageprops":{"smw-semanticdata-status":"1"}}}
    	//33579 is the pageid of queried page, so the iterator just need to return the first thing it gets
    	
    	Iterator allKeys = pages.keys();
    	
    	String key = (String) allKeys.next();//get the first key
    	
		return Integer.parseInt(key);
    }

	public static void main(String[] args) throws JSONException, IOException, ParseException
	{
		//input format: username, password, target page		
				
		if(args.length != 3)
		{
			System.out.println("Input format: username, password, target page.");
			System.exit(0);
		}
		else
		{
			NewsLetterBot mainbot = new NewsLetterBot(args[0], args[1]);
			Utils util = new Utils();
			util.login(mainbot);
			
			if(util.login(mainbot) == true)
			{
				String whichPage = args[2];
				if(getPageId(whichPage) == -1 )//accessing a nonexistent page
				{
					System.out.println("This page does not exist. Please try again.");
					System.exit(0);
				}
				else
				{
					System.out.println("EDITING " + whichPage + "...");
					createNewsletter(mainbot, whichPage);
				}
			}
			else
			{
				System.out.println("Exiting program for login failure.");
				System.exit(0);
			}
			
			


		}
		
		
	}
	
}
