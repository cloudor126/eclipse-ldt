do local _={
	unknownglobalvars={

	}
	--[[table: 0x126ce90]],
	content={
		localvars={
			[1]={
				item={
					type={
						def={
							description="",
							shortdescription="",
							name="table",
							sourcerange={
								min=0,
								max=0
							}
							--[[table: 0x13d7b20]],
							fields={
								fieldname={
									type={
										typename="number",
										tag="primitivetyperef"
									}
									--[[table: 0x111ebe0]],
									description="",
									parent=nil --[[ref]],
									shortdescription="",
									name="fieldname",
									sourcerange={
										min=51,
										max=59
									}
									--[[table: 0x14207f0]],
									occurrences={

									}
									--[[table: 0x14207a0]],
									tag="item"
								}
							--[[table: 0x111ec30]]
							}
							--[[table: 0x10e49e0]],
							tag="recordtypedef"
						}
						--[[table: 0x10e4700]],
						tag="inlinetyperef"
					}
					--[[table: 0x13d7ba0]],
					description="",
					shortdescription="",
					name="tablename",
					sourcerange={
						min=7,
						max=15
					}
					--[[table: 0xf84f00]],
					occurrences={
						[1]={
							sourcerange={
								min=7,
								max=15
							}
							--[[table: 0x127a3b0]],
							definition=nil --[[ref]],
							tag="MIdentifier"
						}
						--[[table: 0x1248ec0]],
						[2]={
							sourcerange={
								min=22,
								max=30
							}
							--[[table: 0x11ae270]],
							definition=nil --[[ref]],
							tag="MIdentifier"
						}
						--[[table: 0x11ae170]],
						[3]={
							sourcerange={
								min=41,
								max=49
							}
							--[[table: 0xfa9680]],
							definition=nil --[[ref]],
							tag="MIdentifier"
						}
					--[[table: 0xfa9580]]
					}
					--[[table: 0xf84eb0]],
					tag="item"
				}
				--[[table: 0x13d7bf0]],
				scope={
					min=0,
					max=0
				}
			--[[table: 0x14209d0]]
			}
		--[[table: 0x1420920]]
		}
		--[[table: 0xfacc20]],
		sourcerange={
			min=1,
			max=10000
		}
		--[[table: 0xfacc70]],
		content={
			[1]=nil --[[ref]],
			[2]={
				sourcerange={
					min=22,
					max=33
				}
				--[[table: 0x11cfa20]],
				left=nil --[[ref]],
				tag="MIndex"
			}
			--[[table: 0x11cf920]],
			[3]={
				sourcerange={
					min=41,
					max=59
				}
				--[[table: 0x11d4790]],
				right="fieldname",
				left=nil --[[ref]],
				tag="MIndex"
			}
		--[[table: 0xfa9780]]
		}
		--[[table: 0xfacbd0]],
		tag="MBlock"
	}
	--[[table: 0x126cee0]],
	tag="MInternalContent"
}
--[[table: 0x126cd90]];
_.content.localvars[1].item.type.def.fields.fieldname.parent=_.content.localvars[1].item.type.def;
_.content.localvars[1].item.occurrences[1].definition=_.content.localvars[1].item;
_.content.localvars[1].item.occurrences[2].definition=_.content.localvars[1].item;
_.content.localvars[1].item.occurrences[3].definition=_.content.localvars[1].item;
_.content.content[1]=_.content.localvars[1].item.occurrences[1];
_.content.content[2].left=_.content.localvars[1].item.occurrences[2];
_.content.content[3].left=_.content.localvars[1].item.occurrences[3];
return _;
end