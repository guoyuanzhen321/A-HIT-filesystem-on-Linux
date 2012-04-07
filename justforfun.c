#include <sys/stat.h>
#include <sys/types.h> 
#include <stdio.h>
#include <dirent.h>
#include <unistd.h>
#include <limits.h>
#include <string.h>



/*void List(char *path)
{
	struct dirent* ent = NULL;
	DIR *pDir; 
	pDir=opendir("/home/steve/oslab");
	ent=readdir(pDir);
	printf("%s\n", ent->d_name);



	while (NULL != (ent=readdir(pDir)))
	{
	if (ent->d_reclen==24)
	  {
	  if (ent->d_type==8)
		printf("%s\n", ent->d_name);
	  else
	    {
		printf("子目录：%s\n",ent->d_name);
		List(ent->d_name);
		printf("返回%s\n",ent->d_name);
	    }
	  }
	}
}*/

int main(int argc, char *argv[])
{
    int mkdirmp3,mkdirwma;
    char files[100][100];
    int count = 0;
    int i,j;
    int paste;
    char dirnow[100];
    char dirthen[100];
    char filename[100];
    char gang[100] = "/";
    char gangmp3[100] = "/mp3/";
    char gangwma[100] = "/wma/";

    mkdirmp3 = mkdir("mp3",0777);
    mkdirwma = mkdir("wma",0777);
    //printf("mkdirectory: %d\n",mkdirectory);
    paste = rename("/home/steve/test/01 Have a Nice Day.mp3","/home/steve/test/tt/01 Have a Nice Day.mp3");
    //printf("paste: %d\n",paste);
    //List(argv[1]);

    DIR * dir;
    struct dirent * ptr;
    
    if(argc==1)
      dir=opendir("./");
    else 
      dir=opendir(argv[1]);
    while((ptr=readdir(dir))!=NULL)
    {
      printf("%s\n",ptr->d_name);
      int length = strlen(ptr->d_name);
      for (i = 0; i < length; i++)
      {
	files[count][i] = ptr->d_name[i];
      }
      files[count][length] = '\0';
      
      if(files[count][length - 1] == '3')
      {
	if(files[count][length - 2] == 'p')
	{
	  if(files[count][length - 3] == 'm')
	  {
	    if(files[count][length - 4] == '.')
	    {
		getcwd(dirnow , sizeof(dirnow)-1 );
		strcat(dirnow,gang);		
		strcat(dirnow,ptr ->d_name);		
		printf("%s\n",dirnow);
		getcwd(dirthen , sizeof(dirthen)-1 );
		strcat(dirthen,gangmp3);		
		strcat(dirthen,ptr ->d_name);		
		printf("%s\n",dirthen);
		paste = rename(dirnow,dirthen);
	    }
	  }
	}
      }

      if(files[count][length - 1] == 'a')
      {
	if(files[count][length - 2] == 'm')
	{
	  if(files[count][length - 3] == 'w')
	  {
	    if(files[count][length - 4] == '.')
	    {
		getcwd(dirnow , sizeof(dirnow)-1 );
		strcat(dirnow,gang);		
		strcat(dirnow,ptr ->d_name);		
		printf("%s\n",dirnow);
		getcwd(dirthen , sizeof(dirthen)-1 );
		strcat(dirthen,gangwma);		
		strcat(dirthen,ptr ->d_name);		
		printf("%s\n",dirthen);
		paste = rename(dirnow,dirthen);
	    }
	  }
	}
      }

      
      count ++;
    }
    /*for(i = 0;i < count;i ++)
    {
	j = 0;	
	while(1)
	{
	if(files[i][j] != '\0')
	  {
		filename[j] = files[i][j];
		j ++;
	  }
	else
	  {
		filename[j] = files[i][j];
		break;
	  }
	}	
	printf("%s\n",filename);
    }*/
    
    closedir(dir);
   
    return 0;
}

