#include <stdio.h>

int main() {
  char in[4];
  char isIn = 0;

  gets (in);
  // fgets (in, 4, stdin);

  if (strcmp (in, "abcd"))
  // if (strcmp (in, "abcd", 4))
    {
      printf ("nespravne heslo\n");
    }
  else
    {
      printf ("spravne heslo, ");
      isIn = 1;
    }

  if (isIn)
    {
      printf ("u in boi");
    }

  return 0;
}